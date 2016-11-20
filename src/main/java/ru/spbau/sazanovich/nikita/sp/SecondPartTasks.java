package ru.spbau.sazanovich.nikita.sp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

public final class SecondPartTasks {

    private static final int NUMBERS_FOR_WANTED_PRECISION = 2000000;
    private static final double SQUARE_SIDE = 1.0;

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths
                .stream()
                .flatMap(path -> {
                    Stream<String> lines = null;
                    try {
                        lines = Files.lines(Paths.get(path));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return lines;
                })
                .filter(line -> line.contains(sequence))
                .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        final Random generator = new Random();
        return Stream
                .generate(() -> new AbstractMap.SimpleEntry<>(generator.nextDouble(), generator.nextDouble()))
                .limit(NUMBERS_FOR_WANTED_PRECISION)
                .mapToInt(e ->
                                Math.pow(e.getKey() - SQUARE_SIDE / 2, 2) + Math.pow(e.getValue() - SQUARE_SIDE / 2, 2)
                                <=
                                Math.pow(SQUARE_SIDE / 2, 2) ? 1 : 0)
                .average()
                .getAsDouble();
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions
                .entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(
                                    entry.getKey(),
                                    entry.getValue().stream().reduce(0, (acc, s) -> acc + s.length(), Integer::sum)))
                .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(AbstractMap.SimpleEntry::getKey)
                .orElse(null);
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey, reducing(0, Map.Entry::getValue, Integer::sum)));
    }
}
