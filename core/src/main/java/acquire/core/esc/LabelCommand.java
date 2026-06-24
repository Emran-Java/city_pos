package acquire.core.esc;

/**
 * Esc label
 *
 * @author Janson
 * @date 2023/5/11 16:04
 */
public class LabelCommand {
    public enum FOOT {
        F2(0),
        F5(1);

        private final int value;

        private FOOT(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
} 
