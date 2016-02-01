package net.jokubasdargis.buildtimer

class BuildTimerPluginExtension {
    static enum SortOrder {
        NONE, ASC, DESC
    }

    final static String NAME = "buildTimer";
    final static long DEFAULT_REPORT_ABOVE = 50L;
    final static SortOrder DEFAULT_SORT_ORDER = SortOrder.NONE;
    long reportAbove = DEFAULT_REPORT_ABOVE;
    SortOrder sort = DEFAULT_SORT_ORDER;
}