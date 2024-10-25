public class Movie {
    private String id;
    private String title;
    private int year;
    private String director;
    private String genres;
    private String stars;
    private float rating;

    public Movie(String id, String title, int year, String director, String genres, String stars, float rating) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
        this.rating = rating;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public int getYear() {
        return this.year;
    }

    public String getDirector() {
        return this.director;
    }

    public String getGenres() {
        return this.genres;
    }

    public String getStars() {
        return this.stars;
    }

    public float getRating() {
        return this.rating;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
