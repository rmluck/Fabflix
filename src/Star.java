public class Star {
    private String id;
    private String name;
    private String birthYear;

    public Star(String id, String name, String birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getBirthYear() {
        return this.birthYear;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }
}