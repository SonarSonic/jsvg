/*
 * MIT License
 *
 * Copyright (c) 2021-2024 Jannis Weis
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
package com.github.weisj.jsvg.nodes.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.weisj.jsvg.renderer.RenderContext;

final class StringTextSegment implements TextSegment {
    private final List<String> codepoints;
    private final TextContainer parent;
    private final int index;

    @Nullable
    GlyphRun currentGlyphRun = null;
    @Nullable
    RenderContext currentRenderContext = null;

    public StringTextSegment(@NotNull TextContainer parent, int index, char[] codepoints) {
        this.parent = parent;
        this.index = index;

        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(new CodepointsCharacterIterator(codepoints));
        int start = it.first();
        List<String> characters = new ArrayList<>();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            characters.add(String.copyValueOf(codepoints, start, end - start));
        }
        this.codepoints = characters;
    }

    public @NotNull List<@NotNull String> codepoints() {
        return codepoints;
    }

    public boolean isLastSegmentInParent() {
        return index == parent.children().size() - 1;
    }

    private static final class CodepointsCharacterIterator implements CharacterIterator {
        private final char[] codepoints;
        private int index;

        private CodepointsCharacterIterator(char[] codepoints) {
            this.codepoints = codepoints;
        }

        @Override
        public char first() {
            if (codepoints.length == 0) return DONE;
            index = getBeginIndex();
            return codepoints[index];
        }

        @Override
        public char last() {
            if (codepoints.length == 0) return DONE;
            index = getEndIndex() - 1;
            return codepoints[index];
        }

        @Override
        public char current() {
            if (index == getEndIndex()) return DONE;
            return codepoints[index];
        }

        @Override
        public char next() {
            if (index == getEndIndex()) return DONE;
            return codepoints[index++];
        }

        @Override
        public char previous() {
            if (index == getBeginIndex()) return DONE;
            return codepoints[--index];
        }

        @Override
        public char setIndex(int position) {
            if (position < getBeginIndex() || position > getEndIndex()) {
                throw new IllegalArgumentException("Invalid index");
            }
            index = position;
            return current();
        }

        @Override
        public int getBeginIndex() {
            return 0;
        }

        @Override
        public int getEndIndex() {
            return codepoints.length;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public @NotNull Object clone() {
            CodepointsCharacterIterator it = new CodepointsCharacterIterator(codepoints);
            it.index = index;
            return it;
        }
    }
}
