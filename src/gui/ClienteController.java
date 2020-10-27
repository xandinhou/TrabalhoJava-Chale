package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Cliente;
import model.services.ClienteService;

public class ClienteController implements Initializable, DataChangeListener {

	private ClienteService service;

	@FXML
	private TableView<Cliente> tableViewCliente;

	@FXML
	private TableColumn<Cliente, Integer> tableColumnId;

	@FXML
	private TableColumn<Cliente, String> tableColumnName;

	@FXML
	private TableColumn<Cliente, String> tableColumnRG;

	@FXML
	private TableColumn<Cliente, String> tableColumnEndereco;

	@FXML
	private TableColumn<Cliente, String> tableColumnCidade;

	@FXML
	private TableColumn<Cliente, String> tableColumnBairro;

	@FXML
	private TableColumn<Cliente, String> tableColumnEstado;

	@FXML
	private TableColumn<Cliente, String> tableColumnCEP;

	@FXML
	private TableColumn<Cliente, Date> tableColumnDataNascimento;

	@FXML
	private TableColumn<Cliente, Cliente> tableColumnEDIT;
	
	@FXML
	private TableColumn<Cliente, Cliente> tableColumnREMOVE;


	@FXML
	private Button btNovo;

	private ObservableList<Cliente> obsList;

	@FXML
	public void onBtNovoAction(ActionEvent event) {
		Stage parenteStage = Utils.currentStage(event);
		Cliente obj = new Cliente();
		createDialogForm(obj, "/gui/ClienteForm.fxml", parenteStage);
	}

	public void setClienteService(ClienteService service) {
		this.service = service;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();

	}

	private void initializeNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("Id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("Name"));
		tableColumnRG.setCellValueFactory(new PropertyValueFactory<>("RG"));
		tableColumnEndereco.setCellValueFactory(new PropertyValueFactory<>("Endereco"));
		tableColumnCidade.setCellValueFactory(new PropertyValueFactory<>("Cidade"));
		tableColumnBairro.setCellValueFactory(new PropertyValueFactory<>("Bairro"));
		tableColumnEstado.setCellValueFactory(new PropertyValueFactory<>("Estado"));
		tableColumnCEP.setCellValueFactory(new PropertyValueFactory<>("CEP"));
		tableColumnDataNascimento.setCellValueFactory(new PropertyValueFactory<>("DataNascimento"));

		Stage stage = (Stage) Main.getScene().getWindow();

		tableViewCliente.prefHeightProperty().bind(stage.heightProperty());

	}

	public void updateTableView() {
		if (service == null) {
			throw new IllegalStateException("Servico e nulo");
		}

		List<Cliente> list = service.findAll();

		obsList = FXCollections.observableArrayList(list);
		tableViewCliente.setItems(obsList);
		initEditButtons();
		initRemoveButtons();

	}

	private void createDialogForm(Cliente obj, String absoluteName, Stage parenteStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			ClienteFormController controller = loader.getController();
			controller.setCliente(obj);
			controller.setClienteService(new ClienteService());
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Registro de Cliente");
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false);
			dialogStage.initOwner(parenteStage);
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();

		} catch (IOException e) {
			Alerts.showAlert("IO Exeption", "Erro loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}

	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Cliente, Cliente>() {
			private final Button button = new Button("Editar");

			@Override
			protected void updateItem(Cliente obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> createDialogForm(obj, "/gui/ClienteForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Cliente, Cliente>() {
			private final Button button = new Button("Remover");

			@Override
			protected void updateItem(Cliente obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}
	
	private void removeEntity(Cliente obj) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");

		if (result.get() == ButtonType.OK) {
			if (service == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				service.remove(obj);
				updateTableView();
			}
			catch (DbException e) {
				Alerts.showAlert("Error removing object", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}

}
