package tech.tryu.stream.entity;

public class Hierarchical {
    private String name;
    private First first;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public First getFirst() {
        return first;
    }

    public void setFirst(First first) {
        this.first = first;
    }

    @Override
    public String toString() {
        return "Hierarchical{" +
                "name:'" + name + '\'' +
                ", first:" + first +
                '}';
    }

    //region 多层嵌套结构
    public static class First {
        private Second second;

        private String name;

        public Second getSecond() {
            return second;
        }

        public void setSecond(Second second) {
            this.second = second;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "First{" +
                    "second :" + second +
                    ", name :'" + name + '\'' +
                    '}';
        }
    }

    public static class Second {
        private Third third;

        public Third getThird() {
            return third;
        }

        public void setThird(Third third) {
            this.third = third;
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Second{" +
                    "third :" + third +
                    ", name :'" + name + '\'' +
                    '}';
        }
    }

    public static class Third {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Third{" +
                    "name :'" + name + '\'' +
                    '}';
        }
    }

    //endregion
}
