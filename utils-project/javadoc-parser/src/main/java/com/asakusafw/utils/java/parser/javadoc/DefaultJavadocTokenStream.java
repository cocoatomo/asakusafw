/**
 * Copyright 2011-2017 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.utils.java.parser.javadoc;

import java.util.LinkedList;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * A basic implementation of {@link JavadocTokenStream}.
 */
public class DefaultJavadocTokenStream implements JavadocTokenStream {

    private final JavadocScanner scanner;
    private final LinkedList<Integer> marks;

    /**
     * Creates a new instance.
     * @param scanner the element scanner
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public DefaultJavadocTokenStream(JavadocScanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner"); //$NON-NLS-1$
        }
        this.scanner = scanner;
        this.marks = new LinkedList<>();
    }

    @Override
    public JavadocToken peek() {
        int index = scanner.getIndex();
        try {
            return nextToken();
        } finally {
            scanner.seek(index);
        }
    }

    @Override
    public JavadocToken lookahead(int k) {
        return scanner.lookahead(k);
    }

    @Override
    public JavadocToken nextToken() {
        while (true) {
            JavadocToken token = scanner.lookahead(0);
            JavadocTokenKind kind = token.getKind();
            if (kind == JavadocTokenKind.LINE_BREAK) {
                int offset = JavadocScannerUtil.countUntilNextLineStart(scanner, 0);
                scanner.consume(offset);
            } else if (kind == JavadocTokenKind.WHITE_SPACES) {
                scanner.consume(1);
            } else {
                scanner.consume(1);
                return token;
            }
        }
    }

    @Override
    public void mark() {
        marks.addFirst(scanner.getIndex());
    }

    @Override
    public void rewind() {
        int mark = fetchMark();
        scanner.seek(mark);
    }

    @Override
    public void discard() {
        fetchMark();
    }

    private Integer fetchMark() {
        if (marks.isEmpty()) {
            throw new IllegalStateException();
        }
        return marks.removeFirst();
    }
}
