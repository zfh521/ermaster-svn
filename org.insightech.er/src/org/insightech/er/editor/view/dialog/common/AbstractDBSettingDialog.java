package org.insightech.er.editor.view.dialog.common;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.insightech.er.Activator;
import org.insightech.er.ResourceString;
import org.insightech.er.common.dialog.AbstractDialog;
import org.insightech.er.common.widgets.CompositeFactory;
import org.insightech.er.common.widgets.ListenerAppender;
import org.insightech.er.db.DBManager;
import org.insightech.er.db.DBManagerFactory;
import org.insightech.er.db.impl.standard_sql.StandardSQLDBManager;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.settings.DBSetting;
import org.insightech.er.preference.PreferenceInitializer;
import org.insightech.er.util.Check;
import org.insightech.er.util.Format;

public abstract class AbstractDBSettingDialog extends AbstractDialog {

	private Text userName;

	private Text password;

	private Combo dbList;

	private Text serverName;

	private Text port;

	private Text dbName;

	private Text url;

	private Text driverClassName;

	private Button settingListButton;

	protected Button settingAddButton;

	protected DBSetting dbSetting;

	protected ERDiagram diagram;

	public AbstractDBSettingDialog(Shell parentShell, ERDiagram diagram) {
		super(parentShell, 2);
		this.diagram = diagram;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialize(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		group.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);

		this.initializeBody(group);

		this.setDBList();
	}

	protected void setDBList() {
		if (this.isOnlyCurrentDatabase()) {
			this.dbList.add(this.diagram.getDatabase());
			this.dbList.select(0);
			this.selectDB();

		} else {
			for (String db : DBManagerFactory.getAllDBList()) {
				this.dbList.add(db);
			}
		}
	}

	protected void initializeBody(Composite group) {
		this.dbList = CompositeFactory.createReadOnlyCombo(this, group,
				"label.database");
		this.dbList.setFocus();

		this.serverName = CompositeFactory.createText(this, group,
				"label.server.name", false);
		this.port = CompositeFactory.createText(this, group, "label.port",
				false);
		this.dbName = CompositeFactory.createText(this, group,
				"label.database.name", false);
		this.userName = CompositeFactory.createText(this, group,
				"label.user.name", false);
		this.password = CompositeFactory.createText(this, group,
				"label.user.password", false);
		this.password.setEchoChar('*');

		this.url = CompositeFactory.createText(null, group, "label.url", 1, -1,
				SWT.BORDER | SWT.READ_ONLY, false);

		this.driverClassName = CompositeFactory.createText(null, group,
				"label.driver.class.name", 1, -1, SWT.BORDER | SWT.READ_ONLY,
				false);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		this.createButton(parent, 0, IDialogConstants.NEXT_LABEL, true);
		this.createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
		this.settingListButton = createButton(
				parent,
				IDialogConstants.OPEN_ID,
				ResourceString.getResourceString("label.load.database.setting"),
				false);
		this.settingAddButton = createButton(parent, IDialogConstants.YES_ID,
				ResourceString
						.getResourceString("label.load.database.setting.add"),
				false);
	}

	public String getDBSName() {
		return this.dbList.getText().trim();
	}

	public String getDBName() {
		return this.dbName.getText().trim();
	}

	public String getServerName() {
		return this.serverName.getText().trim();
	}

	public int getPort() {
		String port = this.port.getText().trim();

		try {
			return Integer.parseInt(port);
		} catch (Exception e) {
			return 0;
		}
	}

	public String getUserName() {
		return this.userName.getText().trim();
	}

	public String getPassword() {
		return this.password.getText().trim();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getErrorMessage() {
		DBManager manager = null;

		String database = this.getDBSName();

		if (!Check.isEmpty(database)) {
			if (StandardSQLDBManager.ID.equals(database)) {
				if (isBlank(this.url)) {
					return "error.url.is.empty";
				}
				if (isBlank(this.driverClassName)) {
					return "error.driver.class.name.is.empty";
				}

			} else {
				manager = DBManagerFactory.getDBManager(this.getDBSName());
				String url = manager.getURL(this.getServerName(), this
						.getDBName(), this.getPort());
				this.url.setText(url);

				if (isBlank(this.serverName) && manager.doesNeedURLServerName()) {
					return "error.server.is.empty";
				}

				if (isBlank(this.port) && manager.doesNeedURLServerName()) {
					return "error.port.is.empty";
				}

				if (isBlank(this.dbName) && manager.doesNeedURLDatabaseName()) {
					return "error.database.name.is.empty";
				}
			}
		}

		if (this.settingAddButton != null) {
			this.settingAddButton.setEnabled(false);
		}

		if (isBlank(this.dbList)) {
			return "error.database.not.selected";
		}

		String text = this.port.getText();

		if (!text.equals("")) {
			try {
				int port = Integer.parseInt(text);
				if (port < 0) {
					return "error.port.zero";
				}

			} catch (NumberFormatException e) {
				return "error.port.degit";
			}
		}

		if (isBlank(this.userName)) {
			return "error.user.name.is.empty";
		}

		if (this.settingAddButton != null) {
			this.settingAddButton.setEnabled(true);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setData() {
		if (this.dbSetting != null) {
			String database = this.dbSetting.getDbsystem();
			this.dbList.setText(database);

			this.selectDB();

			this.serverName.setText(Format.null2blank(this.dbSetting
					.getServer()));
			this.port.setText(String.valueOf(this.dbSetting.getPort()));
			this.dbName
					.setText(Format.null2blank(this.dbSetting.getDatabase()));
			this.userName.setText(Format.null2blank(this.dbSetting.getUser()));
			this.password.setText(Format.null2blank(this.dbSetting
					.getPassword()));
			this.url.setText(Format.null2blank(this.dbSetting.getUrl()));
			this.driverClassName.setText(Format.null2blank(this.dbSetting
					.getDriverClassName()));

			if (!Check.isEmpty(database)
					&& !StandardSQLDBManager.ID.equals(database)) {
				DBManager manager = DBManagerFactory.getDBManager(this
						.getDBSName());
				String url = manager.getURL(this.getServerName(), this
						.getDBName(), this.getPort());
				this.url.setText(url);

				String driverClassName = manager.getDriverClassName();
				this.driverClassName.setText(driverClassName);
			}
		}
	}

	@Override
	protected int getErrorLine() {
		return 2;
	}

	/**
	 * dbSetting ���擾���܂�.
	 * 
	 * @return dbSetting
	 */
	public DBSetting getDbSetting() {
		return this.dbSetting;
	}

	private void selectDB() {
		String database = this.getDBSName();
		if (!Check.isEmpty(database)) {
			DBManager dbManager = DBManagerFactory.getDBManager(database);

			if (StandardSQLDBManager.ID.equals(dbManager.getId())) {
				this.port.setEnabled(false);
				this.serverName.setEnabled(false);
				this.dbName.setEnabled(false);
				this.url.setEditable(true);
				this.driverClassName.setEditable(true);

			} else {
				this.dbName.setEnabled(true);
				this.url.setEditable(false);
				this.driverClassName.setEditable(false);
				this.driverClassName.setText(dbManager.getDriverClassName());

				if (dbManager.doesNeedURLServerName()) {
					this.port.setText(String
							.valueOf(dbManager.getDefaultPort()));
					this.port.setEnabled(true);
					this.serverName.setEnabled(true);

				} else {
					this.port.setEnabled(false);
					this.serverName.setEnabled(false);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addListener() {
		super.addListener();

		this.dbList.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selectDB();
				validate();
			}
		});

		ListenerAppender.addModifyListener(this.serverName, this);
		ListenerAppender.addModifyListener(this.port, this);
		ListenerAppender.addModifyListener(this.dbName, this);
		ListenerAppender.addModifyListener(this.userName, this);
		ListenerAppender.addModifyListener(this.driverClassName, this);

		this.url.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (StandardSQLDBManager.ID.equals(getDBSName())) {
					validate();
				}
			}
		});

		this.settingListButton.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String database = null;
					if (isOnlyCurrentDatabase()) {
						database = diagram.getDatabase();
					}
					DBSettingListDialog dialog = new DBSettingListDialog(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							database);

					if (dialog.open() == IDialogConstants.OK_ID) {
						dbSetting = dialog.getResult();
						setData();
					}

				} catch (Exception ex) {
					Activator.showExceptionDialog(ex);
				}

			}
		});

		this.settingAddButton.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (validate()) {
						setCurrentSetting();

						IPreferenceStore store = Activator.getDefault()
								.getPreferenceStore();

						int num = store
								.getInt(PreferenceInitializer.DB_SETTING_LIST_NUM);
						num++;
						store.setValue(
								PreferenceInitializer.DB_SETTING_LIST_NUM, num);

						DBSettingListDialog.saveSetting(num, dbSetting);

						Activator
								.showMessageDialog("dialog.message.add.to.connection.list");
					}

				} catch (Exception ex) {
					Activator.showExceptionDialog(ex);
				}
			}
		});
	}

	protected boolean isOnlyCurrentDatabase() {
		return false;
	}

	protected void setCurrentSetting() {
		String database = this.getDBSName();
		String url = "";
		String driverClassName = "";
		String serverName = this.getServerName();
		int port = this.getPort();
		String dbName = this.getDBName();

		if (!Check.isEmpty(database)) {
			if (StandardSQLDBManager.ID.equals(database)) {
				url = this.url.getText().trim();
				driverClassName = this.driverClassName.getText().trim();
				serverName = null;
				port = 0;
				dbName = null;
			}
		}

		this.dbSetting = new DBSetting(database, serverName, port, dbName, this
				.getUserName(), this.getPassword(), url, driverClassName);
		DBSettingListDialog.saveSetting(0, this.dbSetting);
	}
}