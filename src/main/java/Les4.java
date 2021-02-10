import java.util.*;

public class Les4 {
    private static final char DOT_EMPTY = '•';
    private static final char DOT_X = 'X';
    private static final char DOT_O = 'O';
    private static final int DELTA_X[] = {0, 1, 1, 1};//смещение по x для линий ↓↘→↗
    private static final int DELTA_Y[] = {1, 1, 0, -1};//смещение по y для линий ↓↘→↗
    private static int SIZE = 8;
    private static int DOTS_TO_WIN = 4;
    private static char[][] map;
    private static Scanner sc = new Scanner(System.in);
    private static Random rand = new Random();
    private static int[] nextComputerStep = {0, 0, 0};

    public static void main(String[] args) {
        System.out.println("Введите размерность поля");
        SIZE = sc.nextInt();
        System.out.println("Введите количество фишек для победы (длину непрерывной линии)");
        DOTS_TO_WIN = sc.nextInt();

        initMap();
        printMap();
        while (true) {
            humanTurn();
            printMap();
            if (checkWin(DOT_X)) {
                System.out.println("Победил человек");
                break;
            }
            if (isMapFull()) {
                System.out.println("Ничья");
                break;
            }
            aiTurn();
            printMap();
            if (checkWin(DOT_O)) {
                System.out.println("Победил Искуственный Интеллект");
                break;
            }
            if (isMapFull()) {
                System.out.println("Ничья");
                break;
            }
        }
        System.out.println("Игра закончена");
    }

    /**
     * Задание 2.Переделать проверку победы, чтобы она не была реализована просто набором условий, например, с использованием циклов.
     * Задание 3.* Попробовать переписать логику проверки победы, чтобы она работала для поля 5х5 и количества фишек 4.
     * Очень желательно не делать это просто набором условий для каждой из возможных ситуаций;
     * <p>
     * Проверяет есть ли победитель в текущем состоянии для игрока, который ходит символом symb,
     * одновременно рассчитывает следующий блокирующий ход компьютера
     *
     * @param symb символ, для которого проверяется победа
     * @return
     */
    public static boolean checkWin(char symb) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                if (map[i][j] != symb)
                    continue;
                boolean lineWasBreak[] = {false, false, false, false};
                int lineSizes[] = {0, 0, 0, 0};//длины непрерывных линий ↓↘→↗ из точки map[i][j]

                for (int k = 0; k < DOTS_TO_WIN; k++)
                    for (int z = 0; z < lineSizes.length; z++)
                        if (!lineWasBreak[z] && //линия не была прервана
                                isCellValid(j + DELTA_Y[z] * k, i + DELTA_X[z] * k)// точка в пределах поля
                                && map[i + DELTA_X[z] * k][j + DELTA_Y[z] * k] == symb)// символ равен symb
                            lineSizes[z]++;
                        else
                            lineWasBreak[z] = true;

                for (int z = 0; z < lineSizes.length; z++)
                    if (lineSizes[z] >= DOTS_TO_WIN)
                        return true;

                if (symb == DOT_X) {
                    int pointNew[] = getPotentPointAndLineLength(i, j, lineSizes, nextComputerStep[2]);
                    if (pointNew[2] > nextComputerStep[2])
                        System.arraycopy(pointNew, 0, nextComputerStep, 0, pointNew.length);
                }
            }
        return false;
    }

    /**
     * Задание 4.*** Доработать искусственный интеллект, чтобы он мог блокировать ходы игрока.
     * <p>
     * Проверяет является ли какая-нибудь из линий ↓↘→↗ идущих из точки map[i][j] более опасной
     * сравнивая её длину с длиной текущей опасной линии lineLength
     *
     * @param i          координана х точки
     * @param j          координата у точки
     * @param lineSizes  длины линий ↓↘→↗ идущих из точки map[i][j]
     * @param lineLength длина текущей опасной линии
     * @return массив с координатами блокирующей точки и длину линии которая будет блокирована
     */
    public static int[] getPotentPointAndLineLength(int i, int j, int lineSizes[], int lineLength) {
        int[] pointAndLength = {0, 0, 0};
        int delta2 = 2;
        for (int z = 0; z < lineSizes.length; z++) {
            //координаты точки продлевающей линию влево
            int prevJ = j + DELTA_Y[z] * (-1);
            int prevI = i + DELTA_X[z] * (-1);

            //координаты точки продлевающей линию вправо
            int postJ = j + DELTA_Y[z] * lineSizes[z];
            int postI = i + DELTA_X[z] * lineSizes[z];

            // проверка случая пустоты между двумя Х (Х•Х) в начале линии
            boolean isHasHoleBeforeBeginLine = lineSizes[z] > lineLength - delta2 &&
                    isCellEmpty(prevJ, prevI)//следующая точка пустая, а через одну Х
                    && isCellDotX(j + DELTA_Y[z] * (-delta2), i + DELTA_X[z] * (-delta2));
            // проверка случая пустоты между двумя Х (Х•Х) в конце линии
            boolean isHasHoleAfterEndLine = lineSizes[z] > lineLength - 2 &&
                    isCellEmpty(postJ, postI)//следующая точка пустая, а через одну Х
                    && isCellDotX(j + DELTA_Y[z] * (lineSizes[z] + 1), i + DELTA_X[z] * (lineSizes[z] + 1));
            //длина линии больше lineLength и можно продлить линию влево
            boolean lengthWillBeMoreIfPutInBeginLine = lineSizes[z] > lineLength && isCellEmpty(prevJ, prevI);
            //длина линии больше lineLength и можно продлить линию вправо
            boolean lengthWillBeMoreIfPutInEndLine = lineSizes[z] > lineLength && isCellEmpty(postJ, postI);

            if (isHasHoleAfterEndLine || lengthWillBeMoreIfPutInEndLine) {
                pointAndLength[0] = postI;
                pointAndLength[1] = postJ;
                lineLength = lineSizes[z] + (isHasHoleAfterEndLine ? 1 : 0);
            }
            if (isHasHoleBeforeBeginLine || lengthWillBeMoreIfPutInBeginLine) {
                pointAndLength[0] = prevI;
                pointAndLength[1] = prevJ;
                lineLength = lineSizes[z] + (isHasHoleBeforeBeginLine ? 1 : 0);
            }
        }
        pointAndLength[2] = lineLength;
        return pointAndLength;
    }

    public static boolean isMapFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (map[i][j] == DOT_EMPTY) return false;
            }
        }
        return true;
    }

    public static void aiTurn() {
        if (isCellEmpty(nextComputerStep[1], nextComputerStep[0]))
            System.out.println("Компьютер противопоставил точку " + (nextComputerStep[1] + 1) + " " + (nextComputerStep[0] + 1));
        else {
            do {
                nextComputerStep[0] = rand.nextInt(SIZE);
                nextComputerStep[1] = rand.nextInt(SIZE);
            } while (!isCellEmpty(nextComputerStep[1], nextComputerStep[0]));
            System.out.println("Компьютер походил случайно в точку " + (nextComputerStep[1] + 1) + " " + (nextComputerStep[0] + 1));
        }
        map[nextComputerStep[0]][nextComputerStep[1]] = DOT_O;
        nextComputerStep[2] = 0;
    }

    public static void humanTurn() {
        int x, y;
        do {
            System.out.println("Введите координаты в формате X Y");
            x = sc.nextInt() - 1;
            y = sc.nextInt() - 1;
        } while (!isCellEmpty(x, y));
        map[y][x] = DOT_X;
    }

    public static boolean isCellEmpty(int x, int y) {
        return (isCellValid(x, y) && map[y][x] == DOT_EMPTY);
    }

    public static boolean isCellDotX(int x, int y) {
        return (isCellValid(x, y) && map[y][x] == DOT_X);
    }

    public static boolean isCellValid(int x, int y) {
        return !(x < 0 || x >= SIZE || y < 0 || y >= SIZE);
    }

    public static void initMap() {
        map = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                map[i][j] = DOT_EMPTY;
            }
        }
    }

    public static void printMap() {
        for (int i = 0; i <= SIZE; i++)
            System.out.print(i + " ");

        System.out.println();
        for (int i = 0; i < SIZE; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < SIZE; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

