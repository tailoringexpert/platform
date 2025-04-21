package eu.tailoringexpert;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public class SortTest {

    private Comparator<String> compator;

    @BeforeEach
    void setup() {
        this.compator = new PostionComparator();
    }
    @Test
    void doit() {
        List<String> strList = new ArrayList<>();
        strList.add("a".getBytes().toString());
        strList.add("b".getBytes().toString());
        strList.add("aa".getBytes().toString());
        strList.add("e".getBytes().toString());

        //using Collections.sort() to sort ArrayList
        Collections.sort(strList);
        for (String str : strList) System.out.print(" " + str);
    }

    @Test
    void test1_1gleich2() {
        // arrange

        // act
        int actual = compator.compare("a", "a");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(0);


    }
    @Test
    void test1_1kleiner2() {
        // arrange

        // act
        int actual = compator.compare("a", "b");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(-1);


    }

    @Test
    void test1_2kleiner1() {
        // arrange

        // act
        int actual = compator.compare("b", "a");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(1);


    }

    @Test
    void test1_1kuerzer2() {
        // arrange

        // act
        int actual = compator.compare("b", "aa");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(-1);


    }

    @Test
    void test1_1gelichlan22() {
        // arrange

        // act
        int actual = compator.compare("ab", "a");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(1);


    }



    public static class PostionComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            int lengthO1 = o1.length();
            int lengthO2 = o2.length();

            if ( lengthO1 == 1 && lengthO1 == lengthO2) {
                return o1.compareToIgnoreCase(o2);
            }

            if (lengthO1 != lengthO2) {
                return Integer.compare(lengthO1,lengthO2);
            }


            return 0;
        }
    }
}
