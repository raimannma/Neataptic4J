package version1;

public enum Selection {
    TOURNAMENT {
        @Override
        public double probability() {
            return 0.5;
        }

        @Override
        public int size() {
            return 5;
        }
    },
    FITNESS_PROPORTIONATE,
    POWER {
        @Override
        public int power() {
            return 4;
        }
    };

    public int power() {
        return 0;
    }

    public int size() {
        return 0;
    }

    public double probability() {
        return 0;
    }
}
