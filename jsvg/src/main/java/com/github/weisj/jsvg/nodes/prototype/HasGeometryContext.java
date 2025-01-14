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
package com.github.weisj.jsvg.nodes.prototype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.weisj.jsvg.attributes.Coordinate;
import com.github.weisj.jsvg.attributes.transform.TransformBox;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.filter.Filter;

public interface HasGeometryContext extends Transformable, HasClip, HasFilter {

    interface ByDelegate extends HasGeometryContext {

        @NotNull
        HasGeometryContext geometryContextDelegate();

        @Override
        default @Nullable ClipPath clipPath() {
            return geometryContextDelegate().clipPath();
        }

        @Override
        default @Nullable Mask mask() {
            return geometryContextDelegate().mask();
        }

        @Override
        default @Nullable Filter filter() {
            return geometryContextDelegate().filter();
        }

        @Override
        default @Nullable TransformValue transform() {
            return geometryContextDelegate().transform();
        }

        @Override
        default TransformBox transformBox() {
            return geometryContextDelegate().transformBox();
        }

        @Override
        default @NotNull Coordinate<LengthValue> transformOrigin() {
            return geometryContextDelegate().transformOrigin();
        }
    }
}
