import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private TextField callBox, opBox, dateBox, timeBox, freqBox, rstsBox, rstrBox, myPota, otherPota;
    @FXML
    private CheckBox potaMode;

    @FXML
    private Button utcButton, enterButton;

    @FXML
    private ChoiceBox<String> modeBox, bandBox;

    private File log;
    private PrintWriter out;
    private LocalDateTime time;

    private final DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("HHmmss");
    private final DateTimeFormatter fileDate = DateTimeFormatter.ofPattern("yyyyMMdd");
    private boolean isPota = false;

    /**
     * Creates a new ADI file specified by the user via a file chooser
     */
    @FXML
    private void newFile() {

        // Detach from current file
        log = null;

        // New file chooser
        FileChooser chooser = new FileChooser();

        // Set window title for the file chooser
        chooser.setTitle("Create Log File");

        // Set ADI as the file extension
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ADIF Files", "*.adi"));

        try{
            // Show window
            log = chooser.showSaveDialog(null);

            // Create the file if it doesn't exist yet
            log.createNewFile();

            // Attach a printwriter to the new file
            out = new PrintWriter(log);

            // Enable user input
            enableInput();

        } catch (IOException e) {

            // Error creating file
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Creating File");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (NullPointerException e) {

            // No file selected
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Null Pointer");
            alert.setContentText(e + "\nMost likely means no file was selected");
            alert.showAndWait();
        }
    }

    /**
     * Loads an existing ADI file specified by the user via a file chooser
     * Will simply append to the selected file
     */
    @FXML
    private void loadFile() {

        // Detach from current file
        log = null;

        // Create file chooser
        FileChooser chooser = new FileChooser();

        // Set window title for the file chooser
        chooser.setTitle("Create Log File");

        // Set ADI as the file extension
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ADIF Files", "*.adi"));

        try{
            // Show window
            log = chooser.showOpenDialog(null);

            // Create the file if it doesn't exist yet
            log.createNewFile();

            // Attach a printwriter to the new file
            out = new PrintWriter(new FileWriter(log, true));

            // Enable user input fields
            enableInput();

        } catch (IOException e) {

            // Error opening file
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Creating File");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (NullPointerException e) {

            // No file selected
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Null Pointer");
            alert.setContentText(e + "\nMost likely means no file was selected");
            alert.showAndWait();
        }
    }

    /**
     * Get current date and time in UTC
     */
    @FXML
    private void getDate() {

        // Log time in UTC using system clock
        time = LocalDateTime.now(Clock.system(ZoneOffset.UTC));

        // Time formatter for user display
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Update fields
        dateBox.setText(time.toLocalDate().toString());
        timeBox.setText(displayFormatter.format(time));
    }

    /**
     * Add the current information to the log; will not log if some
     * required information isn't present.
     * For POTA, basic information is sufficient for logging but there
     * is the option to log your park and any possible Park to Park contacts
     */
    @FXML
    private void addLog() {
        StringBuilder builder = new StringBuilder();

        // Logic
        boolean valid = !(callBox.getText().isEmpty() || opBox.getText().isEmpty() || dateBox.getText().isEmpty() || timeBox.getText().isEmpty() || freqBox.getText().isEmpty() || rstsBox.getText().isEmpty() || rstrBox.getText().isEmpty() || modeBox.getValue().isEmpty() || bandBox.getValue().isEmpty());

        // If all req'd inputs are present
        if(valid && log != null) {

            // Format other station's callsign
            builder.append(String.format("<CALL:%d>%s\n", callBox.getText().length(), callBox.getText()));

            // Format user's callsign
            builder.append(String.format("\t<OPERATOR:%d>%s\n", opBox.getText().length(), opBox.getText()));

            // Format band info
            builder.append(String.format("\t<BAND:%d>%s\n", bandBox.getValue().length(), bandBox.getValue()));

            // Format mode info
            builder.append(String.format("\t<MODE:%d>%s\n", modeBox.getValue().length(), modeBox.getValue()));

            // Format date info
            builder.append(String.format("\t<QSO_DATE:%d>%s\n", fileDate.format(time).length(), fileDate.format(time)));

            // Format time info
            builder.append(String.format("\t<TIME_ON:%d>%s\n",fileTime.format(time).length(), fileTime.format(time)));

            // Format frequency
            builder.append(String.format("\t<FREQ:%d>%s\n", freqBox.getText().length(), freqBox.getText()));

            // Format RST Received
            builder.append(String.format("\t<RST_RCVD:%d>%s\n", rstrBox.getText().length(), rstrBox.getText()));

            // Format RST Sent
            builder.append(String.format("\t<RST_SENT:%d>%s\n", rstsBox.getText().length(), rstsBox.getText()));

            // POTA check: determine if in pota mode and if your park is specified
            // Logs are still valid without pota specified though so non-fatal warning
            // will be shown if stuff isn't present but will still be logged
            if(isPota && !myPota.getText().isEmpty()) {

                // Append POTA special interest group
                builder.append("\t<MY_SIG:4>POTA\n");

                // Append user's specified park
                builder.append(String.format("\t<MY_SIG_INFO:%d>%s\n",myPota.getText().length(), myPota.getText()));

                // Park to park check
                if(!otherPota.getText().isEmpty()) {

                    // Append special interest group
                    builder.append("\t<SIG:4>POTA\n");

                    // Append other park's identifier
                    builder.append(String.format("\t<MY_SIG_INFO:%d>%s\n",otherPota.getText().length(), otherPota.getText()));
                }
            } else if(isPota) {

                // Alert shown if no park is specified, but still logged
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Non-Fatal Error");
                alert.setContentText("Missing your park specifier, log still should be valid");
                alert.show();
            }

            // End of report
            builder.append("<EOR>\n");

            // Append and flush to file
            out.append(builder.toString());
            out.flush();

            // Clear appropriate fields
            callBox.setText("");
            timeBox.setText("");
            rstsBox.setText("");
            rstrBox.setText("");
            otherPota.setText("");

        } else if(log == null){

            // No file selected error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No file currently selected, use the \"File\" menu in the top-left\nto either create a new log or open an existing log.");
            alert.showAndWait();
        } else {

            // Warning for not all info present
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Non-Fatal Error");
            alert.setContentText("Not all required information is present");
            alert.showAndWait();
        }
    }

    /**
     * Toggle between POTA logging and basic logging
     */
    @FXML
    private void enablePota() {
        if(!isPota) {
            myPota.setDisable(false);
            otherPota.setDisable(false);
            isPota = true;
        } else {
            myPota.setDisable(true);
            otherPota.setDisable(true);
            isPota = false;
        }
    }

    /**
     * Enable the user input fields
     */
    private void enableInput() {
        if(log != null) {
            callBox.setDisable(false);
            opBox.setDisable(false);
            dateBox.setDisable(false);
            timeBox.setDisable(false);
            freqBox.setDisable(false);
            rstsBox.setDisable(false);
            rstrBox.setDisable(false);
            utcButton.setDisable(false);
            enterButton.setDisable(false);
            modeBox.setDisable(false);
            bandBox.setDisable(false);
            potaMode.setDisable(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Add the required options to the mode dropdown menu
        modeBox.getItems().addAll("AM", "CW", "FM", "FT4", "FT8", "SSB", "SSTV");

        // Add the HAM bands to the dropdown menu
        bandBox.getItems().addAll("2200m", "630m", "160m", "80m", "60m", "40m", "30m", "20m", "17m", "15m", "12m", "10m", "6m", "2m", "1.25m", "70cm");

        // Set the dropdown boxes to be null right away
        bandBox.setValue("");
        modeBox.setValue("");
    }
}
