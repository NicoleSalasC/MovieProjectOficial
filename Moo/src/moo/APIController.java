package moo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APIController extends JFrame {

    private String apiKey = "1e06b5db7677d5edf1c81d7f19f93735";
    private List<JsonObject> movies;
    private JPanel panel;

    public APIController() {
        movies = new ArrayList<>();
        panel = new JPanel(new GridLayout(0, 3, 4, 4));

        setTitle("Centro de películas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        JButton searchButton = new JButton("Buscar");
        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(20);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        combinedPanel.add(searchPanel, BorderLayout.NORTH);
        combinedPanel.add(panel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(combinedPanel);
        add(scrollPane);
        searchButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchField.getText().trim().toLowerCase();
                if (!searchTerm.isEmpty()) {
                    try {
                        filterMovies(searchTerm);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(APIController.class.getName()).log(Level.SEVERE,
                                null, ex);
                    }
                } else {
                    loadMovies();
                }
            }
        });

        loadMovies();
    }
    /**
     * Este metodo consume el api, carga las peliculas y crea un JsonArray
     * para tenerlas todas en un array y poder acceder a el después.
     */
    private void loadMovies() {
        try {
            URL url = new URL("https://api.themoviedb.org/3/movie/popular?"
                    + "language=es&api_key=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString()).
                    getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            movies.clear();

            for (int i = 0; i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                movies.add(movie);

                String posterPath = movie.get("poster_path").getAsString();

                String posterUrl = "https://image.tmdb.org/t/p/w185" + posterPath;

                ImageIcon imageIcon = new ImageIcon(new URL(posterUrl));
                Image image = imageIcon.getImage();
                Image newImage = image.getScaledInstance(150, 225,
                        Image.SCALE_SMOOTH);
                imageIcon = new ImageIcon(newImage);

                JPanel moviePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                JLabel imageLabel = new JLabel(imageIcon);
                moviePanel.add(imageLabel);

                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showMovieDetails(movie, apiKey);
                    }
                });

                panel.add(moviePanel);
            }

            revalidate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Este metodo sirve para la busqueda de peliculas por el titulo, filtra
     * peliculas si estas no están dentro de la búsqueda
     * @param searchTerm Este parametro es lo que se pone en el cuadro de
     * busqueda
     * @throws MalformedURLException Lanza una excepcion por si se ha 
     * generado incorrectamente el url para acceder a la imagen de la 
     * pelicula
     */
    private void filterMovies(String searchTerm) throws MalformedURLException {
        List<JsonObject> filteredMovies = new ArrayList<>();
        for (JsonObject movie : movies) {
            String title = movie.get("Titulo").getAsString().toLowerCase();
            if (title.contains(searchTerm)) {
                filteredMovies.add(movie);
            }
        }

        panel.removeAll();

        for (JsonObject movie : filteredMovies) {
            String posterPath = movie.get("poster_path").getAsString();
            String posterUrl = "https://image.tmdb.org/t/p/w185" + posterPath;

            ImageIcon imageIcon = new ImageIcon(new URL(posterUrl));
            Image image = imageIcon.getImage();
            Image newImage = image.getScaledInstance(150, 225, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(newImage);

            JPanel moviePanel = new JPanel();
            JLabel imageLabel = new JLabel(imageIcon);
            moviePanel.add(imageLabel);

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showMovieDetails(movie, apiKey);
                }
            });

            panel.add(moviePanel);
        }

        revalidate();
        repaint();
    }
    
    /**
     * Este metodo muestra los detalles de cada pelicula, como la descripción,
     * el trailer y el reparto
     * @param movie Recibe movie para poder acceder al titulo y descipcion
     * anteriormente consumida en el api
     * @param apiKey Recibe el apiKey para poder acceder al api correspondiente.
     */
    private void showMovieDetails(JsonObject movie, String apiKey) {
        String title = movie.get("Titulo").getAsString();
        String overview = movie.get("overview").getAsString();

        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setPreferredSize(new Dimension(400, 300));

        JTextArea overviewTextArea = new JTextArea(overview, 7, 7);
        overviewTextArea.setLineWrap(true);
        overviewTextArea.setWrapStyleWord(true);
        overviewTextArea.setEditable(false);
        overviewTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 16));

        detailsPanel.add(new JScrollPane(overviewTextArea), BorderLayout.CENTER);

        JButton trailerButton = new JButton("Ver Tráiler");
        JButton castButton = new JButton("Ver Reparto");

        trailerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(detailsPanel, "Abrir tráiler de "
                        + title, "Tráiler", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        castButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JOptionPane.showMessageDialog(detailsPanel, "Abrir reparto de "
                        + title, "Reparto", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(trailerButton);
        buttonPanel.add(castButton);

        detailsPanel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, detailsPanel, title,
                JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            APIController frame = new APIController();
            frame.setVisible(true);
        });
    }
}
