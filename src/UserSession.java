public class UserSession {
    private int currentPage;
    private int moviesPerPage;
    private String searchQuery;
    private String titleSortDirection;
    private String ratingSortDirection;
    private String sortPriority;

    public UserSession() {
        this.currentPage = 1;
        this.moviesPerPage = 10;
        this.searchQuery = "";
        this.titleSortDirection = "desc";
        this.ratingSortDirection = "desc";
        this.sortPriority = "r";
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getMoviesPerPage() {
        return moviesPerPage;
    }

    public void setMoviesPerPage(int moviesPerPage) {
        this.moviesPerPage = moviesPerPage;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getTitleSortDirection() {
        return titleSortDirection;
    }

    public void setTitleSortDirection(String titleSortDirection) {
        this.titleSortDirection = titleSortDirection;
    }

    public String getRatingSortDirection() {
        return ratingSortDirection;
    }

    public void setRatingSortDirection(String ratingSortDirection) {
        this.ratingSortDirection = ratingSortDirection;
    }

    public String getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(String sortPriority) {
        this.sortPriority = sortPriority;
    }
}
