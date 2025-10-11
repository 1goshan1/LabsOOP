package ru.ssau.tk.cheefkeef.laba2.functions;

import ru.ssau.tk.cheefkeef.laba2.exceptions.InterpolationException;

import java.util.Iterator;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable { // a lot of explanation so I'll change to русский, но вообще комменты - это уточнение задания
    // чтобы защита легче пошла
    private static class Node {
        public Node next;
        public Node prev;
        public double x;
        public double y;

        public Node(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private Node head;
    protected int count; // защищённое поле, как в ArrayTabulatedFunction

    // Приватный метод добавления узла в конец
    private void addNode(double x, double y) {
        Node newNode = new Node(x, y);
        if (head == null) {
            // Пустой список: делаем циклический узел
            head = newNode;
            head.next = head;
            head.prev = head;
        } else {
            Node last = head.prev; // последний узел в циклическом списке
            last.next = newNode;
            head.prev = newNode;
            newNode.prev = last;
            newNode.next = head;
        }
        count++;
    }

    // Конструктор 1: из двух массивов
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Length must be at least 2");
        }
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        AbstractTabulatedFunction.checkSorted(xValues);

        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // Конструктор 2: из функции и интервала
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Count must be at least 2");
        }
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if (xFrom == xTo) {
            // Все точки одинаковые
            double y = source.apply(xFrom);
            for (int i = 0; i < count; i++) {
                addNode(xFrom, y);
            }
        } else {
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step;
                double y = source.apply(x);
                addNode(x, y);
            }
        }
    }

    // Вспомогательный метод: получить узел по индексу
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }

        Node current;
        if (index <= count / 2) {
            // Идём от головы вперёд
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            // Идём от головы назад (через prev)
            current = head;
            for (int i = 0; i < count - index; i++) {
                current = current.prev;
            }
        }
        return current;
    }

    // Реализация методов интерфейса TabulatedFunction

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x == x) { // точное сравнение double (как в условии)
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.y == y) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return head.x;
    }

    @Override
    public double rightBound() {
        return head.prev.x;
    }

    // Реализация абстрактных методов из AbstractTabulatedFunction

    @Override
    protected int floorIndexOfX(double x) {
        if (x < leftBound()) {
            throw new IllegalArgumentException("X is less than left bound");
        }
        if (x >= rightBound()) {
            return count - 1;
        }

        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (current.x <= x && x < current.next.x) {
                return i;
            }
            current = current.next;
        }
        // На случай, если x == rightBound(), но из-за погрешности не попало
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        // count > 2 v konstruktore
        double x0 = getX(0);
        double x1 = getX(1);
        double y0 = getY(0);
        double y1 = getY(1);
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }

    @Override
    protected double extrapolateRight(double x) {
        // count > 2 v konstruktore
        int n = count - 1;
        double xn_1 = getX(n - 1);
        double xn = getX(n);
        double yn_1 = getY(n - 1);
        double yn = getY(n);
        return yn_1 + (yn - yn_1) * (x - xn_1) / (xn - xn_1);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        // count > 2 v konstruktore
        double x0 = getX(floorIndex);
        double x1 = getX(floorIndex + 1);

        if (!(x0 <= x && x <= x1)) {
            throw new InterpolationException("Illegal x value");
        }

        double y0 = getY(floorIndex);
        double y1 = getY(floorIndex + 1);
        return interpolate(x, x0, x1, y0, y1);
    }

    @Override
    public void insert(double x, double y) {
        if (head == null) {
            // Список пуст — просто добавляем узел
            addNode(x, y);
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
            return;
        }

        // Проходим по списку, чтобы найти подходящее место
        Node current = head;
        do {
            if (current.x == x) {
                // Заменяем значение y, если x уже существует
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
                return;
            }
            current = current.next;
        } while (current != head);

        // Если x больше всех — добавляем в конец (после last)
        // Но в циклическом списке "конец" — это head.prev
        // Однако, если мы дошли до head снова, значит x >= последнего
        // Проверим, не равен ли он последнему
        if (head.prev.x == x) {
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
        }
    }
    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }

        // count > 2 v konstruktore

        if (count <= 2){
            throw new IllegalStateException("Cannot remove point, cause we need at least 2 points");
        }

        Node toRemove = getNode(index);

        // Обновляем связи соседей
        toRemove.prev.next = toRemove.next;
        toRemove.next.prev = toRemove.prev;

        // Если удаляем голову — перемещаем head на следующий узел
        if (toRemove == head) {
            head = toRemove.next;
        }

        count--;

        toRemove.next = null;
        toRemove.prev = null;
    }

    @Override
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException("Iterator is not supported");
    }
}