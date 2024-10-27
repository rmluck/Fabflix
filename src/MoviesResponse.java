import java.util.List;

public class MoviesResponse {
    private List<Movie> movies;
    private int totalMovies;
    private int totalPages;

    public MoviesResponse(List<Movie> movies, int totalMovies, int totalPages) {
        this.movies = movies;
        this.totalMovies = totalMovies;
        this.totalPages = totalPages;
    }

    public List<Movie> getMovies() {
        return this.movies;
    }

    public int getTotalMovies() {
        return this.totalMovies;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public void setTotalMovies(int totalMovies) {
        this.totalMovies = totalMovies;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
