package jext.test;

import jext.Priority;
import org.junit.Test;

import java.util.stream.Stream;

import static jext.Priority.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TestPriority {

    @Test
    public void prioritiesAreSortedBeingHighestFirst() {
        assertThat(Stream.of(Priority.values()).sorted())
            .containsExactly(HIGHEST, HIGHER, NORMAL, LOWER, LOWEST);
    }
}
