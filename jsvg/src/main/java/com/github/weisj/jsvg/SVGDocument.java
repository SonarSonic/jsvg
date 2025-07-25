/*
 * MIT License
 *
 * Copyright (c) 2021-2025 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.jsvg;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.weisj.jsvg.attributes.PreserveAspectRatio;
import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.nodes.SVG;
import com.github.weisj.jsvg.parser.impl.DocumentConstructorAccessor;
import com.github.weisj.jsvg.renderer.*;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.NullPlatformSupport;
import com.github.weisj.jsvg.renderer.PlatformSupport;
import com.github.weisj.jsvg.renderer.animation.Animation;
import com.github.weisj.jsvg.renderer.animation.AnimationState;
import com.github.weisj.jsvg.renderer.awt.AwtComponentPlatformSupport;
import com.github.weisj.jsvg.renderer.impl.*;
import com.github.weisj.jsvg.renderer.impl.context.RenderContextAccessor;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.renderer.output.impl.ShapeOutput;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.view.ViewBox;

public final class SVGDocument {
    private static final boolean DEBUG = false;
    private final @NotNull SVG root;
    private final @NotNull FloatSize size;

    static {
        DocumentConstructorAccessor.setDocumentConstructor(SVGDocument::new);
    }

    private SVGDocument(@NotNull SVG root) {
        this.root = root;
        float em = SVGFont.defaultFontSize();
        this.size = root.sizeForTopLevel(em, SVGFont.exFromEm(em));
    }

    public @NotNull FloatSize size() {
        return size;
    }

    public @NotNull ViewBox viewBox() {
        return root.staticViewBox(size());
    }

    public @NotNull Shape computeShape() {
        return computeShape(null);
    }

    public @NotNull Shape computeShape(@Nullable ViewBox viewBox) {
        Area accumulator = new Area(new Path2D.Float());
        renderWithPlatform(NullPlatformSupport.INSTANCE, new ShapeOutput(accumulator), viewBox);
        return accumulator;
    }

    public boolean isAnimated() {
        return animation().duration() > 0;
    }

    public @NotNull Animation animation() {
        return root.animationPeriod();
    }

    public void render(@Nullable JComponent component, @NotNull Graphics2D g) {
        render(component, g, null);
    }

    public void render(@Nullable Component component, @NotNull Graphics2D graphics2D, @Nullable ViewBox bounds) {
        PlatformSupport platformSupport = component != null
                ? new AwtComponentPlatformSupport(component)
                : NullPlatformSupport.INSTANCE;
        renderWithPlatform(platformSupport, graphics2D, bounds);
    }

    private float computePlatformFontSize(@NotNull PlatformSupport platformSupport, @NotNull Output output) {
        return output.contextFontSize().orElseGet(platformSupport::fontSize);
    }

    public void renderWithPlatform(@NotNull PlatformSupport platformSupport, @NotNull Graphics2D graphics2D,
            @Nullable ViewBox bounds) {
        Output output = Output.createForGraphics(graphics2D);
        renderWithPlatform(platformSupport, output, bounds);
        output.dispose();
    }

    public void renderWithPlatform(@NotNull PlatformSupport platformSupport, @NotNull Output output,
            @Nullable ViewBox bounds) {
        renderWithPlatform(platformSupport, output, bounds, null);
    }

    public void renderWithPlatform(@NotNull PlatformSupport platformSupport, @NotNull Output output,
            @Nullable ViewBox bounds, @Nullable AnimationState animationState) {
        RenderContext context = prepareRenderContext(platformSupport, output, bounds, animationState);

        ViewBox rootVieBox = new ViewBox(root.size(context));

        if (bounds == null) bounds = rootVieBox;

        if (DEBUG) {
            final ViewBox finalBounds = bounds;
            output.debugPaint(g -> {
                g.setColor(Color.RED);
                g.draw(finalBounds);
            });
        }

        AffineTransform rootTransform = PreserveAspectRatio.forDisplay()
                .computeViewportTransform(bounds.size(), rootVieBox);

        RenderContext innerContext = NodeRenderer.setupInnerViewRenderContext(rootVieBox, context, true);

        output.applyClip(bounds);

        innerContext.translate(output, bounds.location());
        innerContext.transform(output, rootTransform);

        // Needed for vector-effects to work properly.
        RenderContextAccessor.Accessor accessor = RenderContextAccessor.instance();
        accessor.setRootTransform(innerContext, output.transform());

        NodeRenderer.renderRootSVG(root, context, output);
    }

    private @NotNull RenderContext prepareRenderContext(
            @NotNull PlatformSupport platformSupport,
            @NotNull Output output,
            @Nullable ViewBox bounds,
            @Nullable AnimationState animationState) {
        float defaultEm = computePlatformFontSize(platformSupport, output);
        float defaultEx = SVGFont.exFromEm(defaultEm);
        AnimationState animState = animationState != null ? animationState : AnimationState.NO_ANIMATION;
        MeasureContext initialMeasure = bounds != null
                ? MeasureContext.createInitial(bounds.size(), defaultEm, defaultEx, animState)
                : MeasureContext.createInitial(root.sizeForTopLevel(defaultEm, defaultEx),
                        defaultEm, defaultEx, animState);
        return RenderContextAccessor.instance().createInitial(platformSupport, initialMeasure);
    }

}
