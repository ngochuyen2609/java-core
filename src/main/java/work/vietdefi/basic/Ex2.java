package work.vietdefi.basic;

public class Ex2 {
    /**
     * Calculates the sum of numbers from 1 to the given input.
     *
     * @param input the max number to sum
     * @return the sum from 1 to input
     */
    public int sum(int input) {
        int total = 0;

        for (int i = 1; i <= input; i++) {
            total += i;
        }

        return total;
    }
}
