public class FabflixParser {

    public static void main(String[] args) {
        try {
            System.out.println("Starting Fabflix XML parser...");

            System.out.println("Parsing movies XML with MainsHandler...");
            MainsHandler.main(new String[]{});
            MainsHandler.main(new String[]{});
            System.out.println("Mains XML parsed successfully.");

            System.out.println("Parsing actors XML with ActorsHandler...");
            ActorsHandler.main(new String[]{});
            System.out.println("Actors XML parsed successfully.");

            System.out.println("Parsing cast XML with CastHandler...");
            CastsHandler.main(new String[]{});
            System.out.println("Cast XML parsed successfully.");

            System.out.println("Fabflix XML parsing process completed.");

        } catch (Exception e) {
            System.err.println("An error occurred during the parsing process: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
