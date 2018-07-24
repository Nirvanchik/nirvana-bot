package org.wikipedia.nirvana.parser.format;

import org.apache.commons.lang3.StringUtils;

/**
 * @author kmorozov
 */
public enum TabFormatDescriptor implements FormatDescriptor {

    TSV_CATSCAN(0, 0, 1, 5, 4, null),
    TSV_CATSCAN2(2, 2, 0, -1, 1, "^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\S+$"),
    TSV_CATSCAN3(2, 2, 0, -1, 1, "^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\d+\\s+\\S+$"),
    TSV_PETSCAN(1, 3, 1, -1, 2, "^\\d+\\s+\\S+\\s+\\d+\\s+(\\S+\\s+)?\\d+\\s+\\d+\\s*$");

    private static final String DEFAULT_LINE_RULE = "^.+$";

    private final int skipLines;
    private final int namespacePos;
    private final int titlePos;
    private final int revidPos;
    private final int idPos;
    // Regex to check validity of line
    private final String lineRule;

    TabFormatDescriptor(int skipLines, int namespacePos, int titlePos, int revidPos, int idPos, String lineRule) {
        this.skipLines = skipLines;
        this.namespacePos = namespacePos;
        this.titlePos = titlePos;
        this.revidPos = revidPos;
        this.idPos = idPos;
        this.lineRule = StringUtils.isEmpty(lineRule) ? DEFAULT_LINE_RULE : lineRule;
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

    public String getLineRule() {
        return lineRule;
    }
}
