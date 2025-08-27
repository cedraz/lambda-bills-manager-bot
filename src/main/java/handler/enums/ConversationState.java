package handler.enums;

public enum ConversationState {
    NONE,
    AWAITING_AMOUNT,
    AWAITING_DESCRIPTION,
    AWAITING_CATEGORY,
    AWAITING_EXPENSE_ID;

    public static ConversationState fromString(String state) {
        for (ConversationState cs : ConversationState.values()) {
            if (cs.name().equalsIgnoreCase(state)) {
                return cs;
            }
        }
        return NONE;
    }
}
