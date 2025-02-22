package com.til.util.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreeTuple<A, B, C>  {
    private A a;
    private B b;
    private C c;
}
