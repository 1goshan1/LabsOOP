package ru.ssau.tk.cheefkeef.laba2.functions;

import ru.ssau.tk.cheefkeef.laba2.exceptions.InterpolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable { // a lot of explanation so I'll change to русский, но вообще комменты - это уточнение задания
    // чтобы защита легче пошла
    private static final Logger logger = LoggerFactory.getLogger(LinkedListTabulatedFunction.class);

    private static class Node implements Serializable {
        @Serial
        private static final long serialVersionUID = -1039477163003185482L;
        public Node next;
        public Node prev;
        public double x;
        public double y;

        public Node(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    @Serial
    private static final long serialVersionUID = 5602953659205222970L;
    private Node head;
    protected int count; // защищённое поле, как в ArrayTabulatedFunction

    // Приватный метод добавления узла в конец
    private void addNode(double x, double y) {
        logger.trace("Adding node: x={}, y={}", x, y);
        Node newNode = new Node(x, y);
        if (head == null) {
            // Пустой список: делаем циклический узел
            head = newNode;
            head.next = head;
            head.prev = head;
            logger.debug("Created first node in empty list");
        } else {
            Node last = head.prev; // последний узел в циклическом списке
            last.next = newNode;
            head.prev = newNode;
            newNode.prev = last;
            newNode.next = head;
            logger.trace("Added node to end of list");
        }
        count++;
        logger.trace("Node added. Total count: {}", count);
    }

    // Конструктор 1: из двух массивов
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        logger.info("Creating LinkedListTabulatedFunction from arrays with length: {}", xValues.length);

        if (xValues.length != yValues.length) {
            logger.error("Arrays length mismatch: x.length={}, y.length={}", xValues.length, yValues.length);
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        if (xValues.length < 2) {
            logger.error("Array length {} is less than minimum required (2)", xValues.length);
            throw new IllegalArgumentException("Length must be at least 2");
        }
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        AbstractTabulatedFunction.checkSorted(xValues);

        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
        logger.info("Successfully created LinkedListTabulatedFunction with {} points", count);
    }

    // Конструктор 2: из функции и интервала
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        logger.info("Creating LinkedListTabulatedFunction from function: xFrom={}, xTo={}, count={}", xFrom, xTo, count);

        if (count < 2) {
            logger.error("Count {} is less than minimum required (2)", count);
            throw new IllegalArgumentException("Count must be at least 2");
        }
        if (xFrom > xTo) {
            logger.warn("xFrom ({}) > xTo ({}), swapping values", xFrom, xTo);
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if (xFrom == xTo) {
            // Все точки одинаковые
            logger.debug("xFrom equals xTo, creating constant function");
            double y = source.apply(xFrom);
            for (int i = 0; i < count; i++) {
                addNode(xFrom, y);
            }
            logger.trace("Constant function: all y values = {}", y);
        } else {
            double step = (xTo - xFrom) / (count - 1);
            logger.debug("Step size: {}", step);
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step;
                double y = source.apply(x);
                addNode(x, y);
                logger.trace("Point {}: x={}, y={}", i, x, y);
            }
        }
        logger.info("Successfully created LinkedListTabulatedFunction from source function");
    }

    // Вспомогательный метод: получить узел по индексу
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            logger.error("Node index out of bounds: index={}, count={}", index, count);
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }

        Node current;
        if (index <= count / 2) {
            // Идём от головы вперёд
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            logger.trace("Found node at index {} by forward traversal", index);
        } else {
            // Идём от головы назад (через prev)
            current = head;
            for (int i = 0; i < count - index; i++) {
                current = current.prev;
            }
            logger.trace("Found node at index {} by backward traversal", index);
        }
        return current;
    }

    // Реализация методов интерфейса TabulatedFunction
    @Override
    public int getCount() {
        logger.trace("Getting count: {}", count);
        return count;
    }

    @Override
    public double getX(int index) {
        double value = getNode(index).x;
        logger.trace("Getting x[{}] = {}", index, value);
        return value;
    }

    @Override
    public double getY(int index) {
        double value = getNode(index).y;
        logger.trace("Getting y[{}] = {}", index, value);
        return value;
    }

    @Override
    public void setY(int index, double value) {
        Node node = getNode(index);
        double oldValue = node.y;
        node.y = value;
        logger.debug("Set y[{}] from {} to {}", index, oldValue, value);
    }

    @Override
    public int indexOfX(double x) {
        if (head == null) {
            logger.debug("List is empty, x={} not found", x);
            return -1;
        }

        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x == x) { // точное сравнение double (как в условии)
                logger.debug("Found x={} at index {}", x, i);
                return i;
            }
            current = current.next;
        }
        logger.debug("x={} not found in list", x);
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        if (head == null) {
            logger.debug("List is empty, y={} not found", y);
            return -1;
        }

        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.y == y) {
                logger.debug("Found y={} at index {}", y, i);
                return i;
            }
            current = current.next;
        }
        logger.debug("y={} not found in list", y);
        return -1;
    }

    @Override
    public double leftBound() {
        if (head == null) {
            logger.error("Cannot get left bound - list is empty");
            throw new IllegalStateException("List is empty");
        }
        double bound = head.x;
        logger.trace("Left bound: {}", bound);
        return bound;
    }

    @Override
    public double rightBound() {
        if (head == null) {
            logger.error("Cannot get right bound - list is empty");
            throw new IllegalStateException("List is empty");
        }
        double bound = head.prev.x;
        logger.trace("Right bound: {}", bound);
        return bound;
    }

    // Реализация абстрактных методов из AbstractTabulatedFunction
    @Override
    protected int floorIndexOfX(double x) {
        if (head == null) {
            logger.error("Cannot find floor index - list is empty");
            throw new IllegalStateException("List is empty");
        }

        if (x < leftBound()) {
            logger.error("X value {} is less than left bound {}", x, leftBound());
            throw new IllegalArgumentException("X is less than left bound");
        }
        if (x >= rightBound()) {
            logger.debug("X value {} is greater than or equal to right bound, returning count-1={}", x, count - 1);
            return count - 1;
        }

        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (current.x <= x && x < current.next.x) {
                logger.debug("Floor index for x={} is {}", x, i);
                return i;
            }
            current = current.next;
        }
        // На случай, если x == rightBound(), но из-за погрешности не попало
        logger.debug("Floor index for x={} is {}", x, count - 1);
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        // count > 2 v konstruktore
        double x0 = getX(0);
        double x1 = getX(1);
        double y0 = getY(0);
        double y1 = getY(1);
        double result = y0 + (y1 - y0) * (x - x0) / (x1 - x0);
        logger.debug("Left extrapolation for x={}: result={}", x, result);
        return result;
    }

    @Override
    protected double extrapolateRight(double x) {
        // count > 2 v konstruktore
        int n = count - 1;
        double xn_1 = getX(n - 1);
        double xn = getX(n);
        double yn_1 = getY(n - 1);
        double yn = getY(n);
        double result = yn_1 + (yn - yn_1) * (x - xn_1) / (xn - xn_1);
        logger.debug("Right extrapolation for x={}: result={}", x, result);
        return result;
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        // count > 2 v konstruktore
        double x0 = getX(floorIndex);
        double x1 = getX(floorIndex + 1);

        if (!(x0 <= x && x <= x1)) {
            logger.error("Interpolation error: x={} not in interval [{}, {}]", x, x0, x1);
            throw new InterpolationException("Illegal x value");
        }

        double y0 = getY(floorIndex);
        double y1 = getY(floorIndex + 1);
        double result = interpolate(x, x0, x1, y0, y1);
        logger.debug("Interpolation for x={} at floorIndex={}: result={}", x, floorIndex, result);
        return result;
    }

    @Override
    public void insert(double x, double y) {
        logger.info("Inserting point: x={}, y={}", x, y);

        if (head == null) {
            // Список пуст — просто добавляем узел
            addNode(x, y);
            logger.debug("Inserted first node in empty list");
            return;
        }

        // Проверяем, не нужно ли вставить в начало (x < всех существующих)
        if (x < head.x) {
            Node newNode = new Node(x, y);
            Node last = head.prev;
            // Вставка перед head
            newNode.next = head;
            newNode.prev = last;
            head.prev = newNode;
            last.next = newNode;
            head = newNode; // Обновляем голову
            count++;
            logger.debug("Inserted node at beginning of list");
            return;
        }

        // Проходим по списку, чтобы найти подходящее место
        Node current = head;
        do {
            if (current.x == x) {
                // Заменяем значение y, если x уже существует
                logger.debug("Point with x={} already exists, updating y value", x);
                current.y = y;
                return;
            }
            if (current.next.x > x) {
                // Нашли интервал: current.x < x < current.next.x
                Node newNode = new Node(x, y);
                newNode.next = current.next;
                newNode.prev = current;
                current.next.prev = newNode;
                current.next = newNode;
                count++;
                logger.debug("Inserted node in middle of list at appropriate position");
                return;
            }
            current = current.next;
        } while (current != head);

        // Если x больше всех — добавляем в конец (после last)
        // Но в циклическом списке "конец" — это head.prev
        // Однако, если мы дошли до head снова, значит x >= последнего
        // Проверим, не равен ли он последнему
        if (head.prev.x == x) {
            logger.debug("Point with x={} exists at end, updating y value", x);
            head.prev.y = y;
        } else {
            // Добавляем после последнего узла
            Node last = head.prev;
            Node newNode = new Node(x, y);
            newNode.next = head;
            newNode.prev = last;
            last.next = newNode;
            head.prev = newNode;
            count++;
            logger.debug("Inserted node at end of list");
        }
    }

    @Override
    public void remove(int index) {
        logger.info("Removing point at index {}", index);

        if (index < 0 || index >= count) {
            logger.error("Index out of bounds: index={}, count={}", index, count);
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }

        // count > 2 v konstruktore
        if (count <= 2){
            logger.error("Cannot remove point - minimum points count reached (current: {})", count);
            throw new IllegalStateException("Cannot remove point, cause we need at least 2 points");
        }

        Node toRemove = getNode(index);

        // Обновляем связи соседей
        toRemove.prev.next = toRemove.next;
        toRemove.next.prev = toRemove.prev;

        // Если удаляем голову — перемещаем head на следующий узел
        if (toRemove == head) {
            head = toRemove.next;
            logger.debug("Removed head node, new head set");
        }

        count--;
        toRemove.next = null;
        toRemove.prev = null;

        logger.debug("Successfully removed point at index {}. New count: {}", index, count);
    }

    @Override
    public Iterator<Point> iterator() {
        logger.trace("Creating iterator for LinkedListTabulatedFunction");
        return new Iterator<Point>() {
            private Node currentNode = head;
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                boolean hasNext = currentNode != null && currentIndex < count;
                logger.trace("Iterator hasNext: {} (currentIndex={}, count={})", hasNext, currentIndex, count);
                return hasNext;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    logger.error("Iterator has no more elements");
                    throw new NoSuchElementException("No more elements in the list");
                }

                Point point = new Point(currentNode.x, currentNode.y);
                currentIndex++;

                if (currentIndex < count) {
                    currentNode = currentNode.next;
                } else {
                    currentNode = null; // Достигли конца списка
                }

                logger.trace("Iterator next: point = ({}, {})", point.x, point.y);
                return point;
            }
        };
    }
}