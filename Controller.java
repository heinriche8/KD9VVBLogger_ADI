import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
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
    private TextField callBox, opBox, dateBox, timeBox, freqBox, rstsBox, rstrBox;

    @FXML
    private Button utcButton, enterButton;

    @FXML
    private ChoiceBox<String> modeBox, bandBox;

    private File log;
    private PrintWriter out;
    private LocalDateTime time;

    private final DateTimeFormatter fileTime = DateTimeFormatter.ofPattern("HHmmss");
    private final DateTimeFormatter fileDate = DateTimeFormatter.ofPattern("yyyyMMdd");

    @FXML
    private void newFile() {
        log = null;

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

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Creating File");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Null Pointer");
            alert.setContentText(e + "\nMost likely means no file was selected");
            alert.showAndWait();
        }

        enableInput();
    }

    @FXML
    private void loadFile() {
        log = null;

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

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Creating File");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Null Pointer");
            alert.setContentText(e + "\nMost likely means no file was selected");
            alert.showAndWait();
        }

        enableInput();
    }

    @FXML
    private void getDate() {
        time = LocalDateTime.now(Clock.system(ZoneOffset.UTC));

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        dateBox.setText(time.toLocalDate().toString());
        timeBox.setText(displayFormatter.format(time));
    }

    @FXML
    private void addLog() {
        StringBuilder builder = new StringBuilder();
        boolean valid = !(callBox.getText().isEmpty() || opBox.getText().isEmpty() || dateBox.getText().isEmpty() || timeBox.getText().isEmpty() || freqBox.getText().isEmpty() || rstsBox.getText().isEmpty() || rstrBox.getText().isEmpty() || modeBox.getValue().isEmpty() || bandBox.getValue().isEmpty());

        if(valid && log != null) {
            builder.append(String.format("<CALL:%d>%s\n", callBox.getText().length(), callBox.getText()));

            builder.append(String.format("\t<OPERATOR:%d>%s\n", opBox.getText().length(), opBox.getText()));

            builder.append(String.format("\t<BAND:%d>%s\n", bandBox.getValue().length(), bandBox.getValue()));

            builder.append(String.format("\t<MODE:%d>%s\n", modeBox.getValue().length(), modeBox.getValue()));

            builder.append(String.format("\t<QSO_DATE:%d>%s\n", fileDate.format(time).length(), fileDate.format(time)));

            builder.append(String.format("\t<TIME_ON:%d>%s\n",fileTime.format(time).length(), fileTime.format(time)));

            builder.append(String.format("\t<FREQ:%d>%s\n", freqBox.getText().length(), freqBox.getText()));

            builder.append(String.format("\t<RST_RCVD:%d>%s\n", rstrBox.getText().length(), rstrBox.getText()));

            builder.append(String.format("\t<RST_SENT:%d>%s\n", rstsBox.getText().length(), rstsBox.getText()));

            builder.append("<EOR>\n");

            out.append(builder.toString());
            out.flush();

            callBox.setText("");
            timeBox.setText("");
            rstsBox.setText("");
            rstrBox.setText("");
        } else if(log == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No file currently selected, use the \"File\" menu in the top-left\nto either create a new log or open an existing log.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Non-Fatal Error");
            alert.setContentText("Not all required information is present");
            alert.showAndWait();
        }
    }

    private void enableInput() {
        if(log != null) {
            callBox.setEditable(true);
            opBox.setEditable(true);
            dateBox.setEditable(true);
            timeBox.setEditable(true);
            freqBox.setEditable(true);
            rstsBox.setEditable(true);
            rstrBox.setEditable(true);
            utcButton.setDisable(false);
            enterButton.setDisable(false);
            modeBox.setDisable(false);
            bandBox.setDisable(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modeBox.getItems().addAll("AM", "CW", "FM", "FT4", "FT8", "SSB", "SSTV");
        bandBox.getItems().addAll("2200m", "630m", "160m", "80m", "60m", "40m", "30m", "20m", "17m", "15m", "12m", "10m", "6m", "2m", "1.25m", "70cm");
        bandBox.setValue("");
        modeBox.setValue("");
    }
}
