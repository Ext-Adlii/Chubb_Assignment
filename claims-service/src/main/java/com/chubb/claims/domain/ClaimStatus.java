package com.chubb.claims.domain;

public enum ClaimStatus {
    SUBMITTED,
    UNDER_REVIEW,
    INFO_REQUESTED,
    SETTLED,
    REJECTED;

    public boolean canTransitionTo(ClaimStatus next) {
        return switch (this) {
            case SUBMITTED -> next == UNDER_REVIEW;
            case UNDER_REVIEW -> next == INFO_REQUESTED || next == SETTLED || next == REJECTED;
            case INFO_REQUESTED -> next == UNDER_REVIEW;
            case SETTLED, REJECTED -> false;
        };
    }
}
