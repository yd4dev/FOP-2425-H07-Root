package h07;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions2;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import org.tudalgo.algoutils.tutor.general.match.BasicStringMatchers;
import org.tudalgo.algoutils.tutor.general.reflections.BasicMethodLink;
import org.tudalgo.algoutils.tutor.general.reflections.BasicTypeLink;
import org.tudalgo.algoutils.tutor.general.reflections.MethodLink;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtMethod;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;

import static h07.ClassReference.NUMBER_EXPRESSION;
import static h07.H07Test.getCtElements;
import static h07.MethodReference.NUMBER_EXPRESSION_EVALUATE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class NumberExpressionFactoryTest {

    @ParameterizedTest
    @JsonParameterSetTest(value = "NumberExpressionFactory_multiply_Simple.json")
    public void multiplicationTableSimple(JsonParameterSet params) throws Throwable {
        performTest_multiplication(params);
    }

    @ParameterizedTest
    @JsonParameterSetTest(value = "NumberExpressionFactory_multiply_Complex.json")
    public void multiplicationTableComplex(JsonParameterSet params) throws Throwable {
        performTest_multiplication(params);
    }

    private static void performTest_multiplication(JsonParameterSet params) throws Throwable {
        int lowerBound = params.getInt("lowerBound");
        int upperBound = params.getInt("upperBound");

        List<Integer> expected = new ArrayList<>();
        params.getRootNode().get("expected").forEach(value -> expected.add(value.intValue()));

        Context context = params.toContext();

        List<Object> actualExpressions = multiplicationTable(lowerBound, upperBound);
        List<Integer> actual = actualExpressions.stream().map(expr -> {
            try {
                return NUMBER_EXPRESSION_EVALUATE.<Integer>invoke(expr.getClass(), expr);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }).toList();

        assertEquals(expected, actual, context, r -> "Returned multiplication table does not match expected.");
    }

    private static List<Object> multiplicationTable(int lowerBound, int upperBound) throws Throwable {
        int numberOfNumbers = upperBound - lowerBound + 1;
        List<Object> baseNumbers = new ArrayList<>(numberOfNumbers);

        for (int i = lowerBound; i <= upperBound; i++) {

            Object expression = NumberConverter.toNumberExpression(i);
            baseNumbers.add(expression);
        }

        MethodLink multiplicationLink = BasicMethodLink.of(Arrays.stream(NumberExpressionFactory.class.getMethods())
            .filter(method -> method.getParameterCount() == 1 && method.getName().equals("multiplicationTable"))
            .findFirst()
            .get());

        if (multiplicationLink == null) {
            throw new RuntimeException("Could not find method NumberExpressionFactory.multiplicationTable()");
        }

        Object returned = multiplicationLink.invokeStatic((Object) (baseNumbers.toArray((Object[]) Array.newInstance(
            NUMBER_EXPRESSION.getLink().reflection(),
            0
        ))));

        try {
            return List.of((Object[]) returned);
        } catch (NullPointerException e) {
            fail(emptyContext(), r -> "multiplicationTable() returned null or an array containing null");
        }
        return null;
    }

    @ParameterizedTest
    @JsonParameterSetTest(value = "NumberExpressionFactory_filter_empty.json")
    public void filter_empty(JsonParameterSet params) throws Throwable {
        performTest_filter(params);
    }

    @ParameterizedTest
    @JsonParameterSetTest(value = "NumberExpressionFactory_filter_true.json")
    public void filter_true(JsonParameterSet params) throws Throwable {
        performTest_filter(params);
    }

    @ParameterizedTest
    @JsonParameterSetTest(value = "NumberExpressionFactory_filter_false.json")
    public void filter_false(JsonParameterSet params) throws Throwable {
        performTest_filter(params);
    }

    @ParameterizedTest
    @JsonParameterSetTest(value = "NumberExpressionFactory_filter_complex.json")
    public void filter_complex(JsonParameterSet params) throws Throwable {
        performTest_filter(params);
    }

    private static void performTest_filter(JsonParameterSet params) throws Throwable {
        List<Integer> inputs = new ArrayList<>();
        params.getRootNode().get("inputs").forEach(value -> inputs.add(value.intValue()));

        Map<Integer, Boolean> filterMappings = new HashMap<>();
        AtomicInteger currentElem = new AtomicInteger();
        params.getRootNode()
            .get("filter")
            .forEach(value -> filterMappings.put(inputs.get(currentElem.getAndIncrement()), value.booleanValue()));

        List<Integer> expected = new ArrayList<>();
        params.getRootNode().get("expected").forEach(value -> expected.add(value.intValue()));

        IntPredicate predicate = filterMappings::get;

        List<Object> baseNumbers = new ArrayList<>();
        inputs.forEach(input -> {
            Object expression = mock(NUMBER_EXPRESSION.getLink().reflection());
            try {
                when(NUMBER_EXPRESSION_EVALUATE.invoke(NUMBER_EXPRESSION.getLink().reflection(), expression)).thenReturn(input);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            baseNumbers.add(expression);
        });

        MethodLink filterLink = BasicTypeLink.of(NumberExpressionFactory.class)
                    .getMethod(BasicStringMatchers.identical("filter"));

        if (filterLink == null) {
            fail(emptyContext(), r -> "Could not find method NumberExpressionFactory.filter()");
        }

        List<Object> actualExpressions = List.of(
            filterLink.invokeStatic(
                baseNumbers.toArray((Object[]) Array.newInstance(NUMBER_EXPRESSION.getLink().reflection(), 0)),
                predicate
            )
        );
        List<Integer> actual = actualExpressions.stream().map(expr -> {
            try {
                return NUMBER_EXPRESSION_EVALUATE.<Integer>invoke(expr.getClass(), expr);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }).toList();

        Context context = contextBuilder()
            .add("inputs", inputs)
            .add("filter", filterMappings)
            .add("expected", expected)
            .build();

        assertEquals(expected, actual, context, r -> "Returned List of expressions does not match expected list.");
    }

    @Test
    public void testMultiplicationTableRequirements() throws Throwable {
        CtMethod<?> multiplicationTable = BasicMethodLink.of(Arrays.stream(NumberExpressionFactory.class.getMethods())
                .filter(method -> method.getParameterCount() == 1 && method.getName().equals("multiplicationTable"))
                .findFirst()
                .get())
            .getCtElement();

        List<CtLambda> lambdas =
            getCtElements(List.of(NumberExpressionFactory.class), CtLambda.class, multiplicationTable).toList();
        boolean hasCorrectLambdas = lambdas.stream()
            .anyMatch(ctLambda -> {
                    if (ctLambda.getBody() == null) {
                        return false;
                    }

                    List<CtLambda> innerLambdas =
                        getCtElements(List.of(NumberExpressionFactory.class), CtLambda.class, ctLambda).toList();

                    return innerLambdas.size() == 1 && innerLambdas.get(0).getBody() == null;
                }
            );

        Assertions2.assertTrue(
            hasCorrectLambdas,
            contextBuilder().add("Lambdas", lambdas.stream().map(CtLambda::toStringDebug).toList()).build(),
            r -> "multiplicationTable() does not use correct lambdas."
        );

        boolean returnsArrayOfLambda =
            multiplicationTable(0, 2).stream().allMatch(it -> it.getClass().getName().contains("$$Lambda"));

        Assertions2.assertTrue(
            returnsArrayOfLambda,
            emptyContext(),
            r -> "multiplicationTable() does not return array of objects created from lambdas."
        );
    }
}
