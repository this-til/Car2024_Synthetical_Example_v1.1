package com.til.problem;

public interface IProblem {

    void solve() throws Exception;

    default String name() {
        return this.getClass().getSimpleName();
    }
}
