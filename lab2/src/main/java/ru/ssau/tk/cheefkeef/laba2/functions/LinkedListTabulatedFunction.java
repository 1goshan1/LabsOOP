package ru.ssau.tk.cheefkeef.laba2.functions;

import java.util.Arrays;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction { // a lot of explanation so I'll change to русский, но вообще комменты - это уточнение задания
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
        if (xValues.length == 0) {
            throw new IllegalArgumentException("Arrays must not be empty");
        }
        // Предполагается, что xValues упорядочены и без дубликатов
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // Конструктор 2: из функции и интервала
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
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
            return -1;
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
        if (count == 1) {
            return getY(0);
        }
        double x0 = getX(0);
        double x1 = getX(1);
        double y0 = getY(0);
        double y1 = getY(1);
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }

    @Override
    protected double extrapolateRight(double x) {
        if (count == 1) {
            return getY(0);
        }
        int n = count - 1;
        double xn_1 = getX(n - 1);
        double xn = getX(n);
        double yn_1 = getY(n - 1);
        double yn = getY(n);
        return yn_1 + (yn - yn_1) * (x - xn_1) / (xn - xn_1);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (count == 1) {
            return getY(0);
        }
        double x0 = getX(floorIndex);
        double x1 = getX(floorIndex + 1);
        double y0 = getY(floorIndex);
        double y1 = getY(floorIndex + 1);
        return interpolate(x, x0, x1, y0, y1);
    }
}