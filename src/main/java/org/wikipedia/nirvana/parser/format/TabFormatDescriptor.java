package org.wikipedia.nirvana.parser.format;

/**
 * @author kmorozov
 *
 */

public enum TabFormatDescriptor implements FormatDescriptor {

    TSV_CATSCAN (0, 0, 1, 5, 4),
    TSV_CATSCAN2 (2, 2, 0, -1, 1),
    TSV_CATSCAN3 (2, 2, 0, -1, 1),
    TSV_PETSCAN (1, 3, 1, -1, 2);

    private final int skipLines;
    private final int namespacePos;
    private final int titlePos;
    private final int revidPos;
    private final int idPos;

    TabFormatDescriptor (int skipLines, int namespacePos, int titlePos, int revidPos, int idPos) {

        this.skipLines = skipLines;
        this.namespacePos = namespacePos;
        this.titlePos = titlePos;
        this.revidPos = revidPos;
        this.idPos = idPos;
    }

    public int getSkipLines() {
        return skipLines;
    }

    public int getNamespacePos() {
        return namespacePos;
    }

    public int getTitlePos() {
        return titlePos;
    }

    public int getRevidPos() {
        return revidPos;
    }

    public int getIdPos() {
        return idPos;
    }
}
