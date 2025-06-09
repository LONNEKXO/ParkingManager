import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


public class ParkingManager {
    private static final String SPOTS_FILE = "liczbamiejsc.txt";
    private static final String OWNERS_FILE = "właściciele.txt";

    private static final String OLD_OWNERS_FILE = "oldowners.txt";
    
    private JFrame loginFrame;
    private JFrame mainFrame;
    private int parkingSpots;


    public ParkingManager() {


        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();

        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Piotr Lonn");



        loadParkingSpots();

        loginFrame = new JFrame("Logowanie");
        loginFrame.setSize(400, 250);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new BorderLayout());
        loginFrame.setLocationRelativeTo(null);

        UIManager.put("OptionPane.background", new Color(40, 40, 40));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Panel.background", new Color(40, 40, 40));
        UIManager.put("Button.background", new Color(60, 60, 60));
        UIManager.put("Button.foreground", Color.WHITE);

        try {
            URL iconUrl = new URL("https://i.imgur.com/ghTWxfW.png"); // Wstaw tutaj URL do swojej ikony
            ImageIcon icon = new ImageIcon(iconUrl);
            loginFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel loginPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        loginPanel.setBackground(new Color(40, 40, 40));

        JLabel userLabel = new JLabel("Login:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        JTextField userText = new JTextField(20);

        JLabel passwordLabel = new JLabel("Hasło:");
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.BOLD));
        passwordLabel.setForeground(Color.WHITE);
        JPasswordField passwordText = new JPasswordField(20);

        JButton loginButton = new JButton("Zaloguj");
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD));
        loginButton.setForeground(Color.WHITE);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String user = userText.getText();
                String password = new String(passwordText.getPassword());
                if (user.equals("admin") && password.equals("admin")) {
                    loginFrame.setVisible(false);
                    openMainWindow();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Nieprawidłowy login lub hasło", "Błąd logowania", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginPanel.add(userLabel);
        loginPanel.add(userText);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordText);
        loginPanel.add(new JLabel());
        loginPanel.add(loginButton);

        loginFrame.add(loginPanel, BorderLayout.CENTER);

        loginFrame.setVisible(true);
    }

    private void openMainWindow() {
        mainFrame = new JFrame("Zarządzanie parkingiem");
        mainFrame.setSize(800, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setLocationRelativeTo(null);

        JLabel madeByLabel = new JLabel("Made by Piotr Lonn", SwingConstants.RIGHT);
        madeByLabel.setForeground(Color.WHITE);
        madeByLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainFrame.add(madeByLabel, BorderLayout.SOUTH);


        mainFrame.getContentPane().setBackground(new Color(40, 40, 40));
        UIManager.put("OptionPane.background", new Color(40, 40, 40));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Panel.background", new Color(40, 40, 40));
        UIManager.put("Button.background", new Color(60, 60, 60));
        UIManager.put("Button.foreground", Color.WHITE);



        try {
            URL iconUrl = new URL("https://i.imgur.com/ghTWxfW.png"); // Wstaw tutaj URL do swojej ikony
            ImageIcon icon = new ImageIcon(iconUrl);
            mainFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }



        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveParkingSpots();
                System.exit(0);
            }
        });

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setBackground(new Color(40, 40, 40));

        JButton assignOwnerButton = new JButton("Nadaj właściciela");
        assignOwnerButton.setPreferredSize(new Dimension(200, 50));
        assignOwnerButton.setForeground(Color.WHITE);
        assignOwnerButton.setFont(assignOwnerButton.getFont().deriveFont(Font.BOLD));

        assignOwnerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();

                JButton clickedButton = (JButton) e.getSource();

                String spotNumberStr = JOptionPane.showInputDialog(mainFrame, "Podaj numer miejsca:");

                if (spotNumberStr == null) {
                    openMainWindow();
                    return;
                }
                if (spotNumberStr.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Musisz podać numer miejsca!", "Błąd", JOptionPane.ERROR_MESSAGE);
                    openMainWindow();
                    return;
                }

                try {
                    int spotNumber = Integer.parseInt(spotNumberStr);

                    if (isSpotOccupied(spotNumber)) {
                        JOptionPane.showMessageDialog(null, "Dane miejsce jest zajęte!", "Błąd", JOptionPane.ERROR_MESSAGE);
                        openMainWindow();
                        return;
                    }

                    openOwnerAssignmentWindow(spotNumber);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Podana wartość nie jest liczbą!", "Błąd", JOptionPane.ERROR_MESSAGE);
                    openMainWindow();
                }
            }
        });






        JButton displayOwnerButton = new JButton("Wyświetl włascicieli");
        displayOwnerButton.setForeground(Color.WHITE);
        displayOwnerButton.setFont(displayOwnerButton.getFont().deriveFont(Font.BOLD));
        displayOwnerButton.setPreferredSize(new Dimension(200, 50));

        displayOwnerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.setVisible(false);
                displayOwners();
            }
        });


        JButton displayOldOwnerButton = new JButton("Wyświetl poprzednich właścicieli");
        displayOldOwnerButton.setForeground(Color.WHITE);
        displayOldOwnerButton.setFont(displayOldOwnerButton.getFont().deriveFont(Font.BOLD));
        displayOldOwnerButton.setPreferredSize(new Dimension(200, 50));

        displayOldOwnerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.setVisible(false);
                displayOldOwners();
            }
        });




        JButton displaySpotsButton = new JButton("Wyświetl miejsca");
        displaySpotsButton.setForeground(Color.WHITE);
        displaySpotsButton.setFont(displayOwnerButton.getFont().deriveFont(Font.BOLD));
        displaySpotsButton.setPreferredSize(new Dimension(200, 50));
        displaySpotsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.setVisible(false);
                displaySpots();
            }
        });

        JButton exitButton = new JButton("Wyjdź");
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(exitButton.getFont().deriveFont(Font.BOLD));
        exitButton.setPreferredSize(new Dimension(200, 50));

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveParkingSpots();
                System.exit(0);
            }
        });

        buttonPanel.add(assignOwnerButton);
        buttonPanel.add(displayOwnerButton);
        buttonPanel.add(displayOldOwnerButton);
        buttonPanel.add(displaySpotsButton);
        buttonPanel.add(exitButton);

        mainFrame.add(buttonPanel);

        mainFrame.setVisible(true);
    }

    private void openOwnerAssignmentWindow(int spotNumber) {

        JFrame ownerFrame = new JFrame("Nadaj właściciela");
        ownerFrame.setSize(600, 400);
        ownerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ownerFrame.setLayout(new BorderLayout());
        ownerFrame.setLocationRelativeTo(null);

        try {
            URL iconUrl = new URL("https://i.imgur.com/ghTWxfW.png");
            ImageIcon icon = new ImageIcon(iconUrl);
            ownerFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }



        ownerFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                openMainWindow();
            }
        });


        JPanel ownerPanel = new JPanel(new GridLayout(8, 2, 5, 5));

        JLabel spotLabel = new JLabel("Numer miejsca:");
        JTextField spotText = new JTextField(String.valueOf(spotNumber));
        spotLabel.setForeground(Color.WHITE);
        spotLabel.setFont(spotLabel.getFont().deriveFont(Font.BOLD));
        spotText.setEditable(false);

        JLabel nameLabel = new JLabel("Imię:");
        JTextField nameText = new JTextField();
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));

        JLabel surnameLabel = new JLabel("Nazwisko:");
        JTextField surnameText = new JTextField();
        surnameLabel.setForeground(Color.WHITE);
        surnameLabel.setFont(surnameLabel.getFont().deriveFont(Font.BOLD));

        JLabel registrationLabel = new JLabel("Nr. rejestracji:");
        JTextField registrationText = new JTextField();
        registrationLabel.setForeground(Color.WHITE);
        registrationLabel.setFont(registrationLabel.getFont().deriveFont(Font.BOLD));

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailText = new JTextField();
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(emailLabel.getFont().deriveFont(Font.BOLD));

        JLabel phoneLabel = new JLabel("Nr. telefonu:");
        JTextField phoneText = new JTextField();
        phoneLabel.setForeground(Color.WHITE);
        phoneLabel.setFont(phoneLabel.getFont().deriveFont(Font.BOLD));

        JLabel expiryDateLabel = new JLabel("Data ważności (RRRR-MM-DD):");
        JTextField expiryDateText = new JTextField();
        expiryDateLabel.setForeground(Color.WHITE);
        expiryDateLabel.setFont(expiryDateText.getFont().deriveFont(Font.BOLD));

        JButton addButton = new JButton("Dodaj");
        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD));
        addButton.setForeground(Color.WHITE);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String spot = spotText.getText();
                String name = nameText.getText();
                String surname = surnameText.getText();
                String registration = registrationText.getText();
                String email = emailText.getText();
                String phone = phoneText.getText();
                String expiryDateStr = expiryDateText.getText();

                int spotNumber;
                try {
                    spotNumber = Integer.parseInt(spot);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ownerFrame, "Nieprawidłowy numer miejsca", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (spotNumber <= 0 || spotNumber > parkingSpots) {
                    JOptionPane.showMessageDialog(ownerFrame, "Nieprawidłowy numer miejsca", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!name.matches("[a-zA-Z]+")) {
                    JOptionPane.showMessageDialog(ownerFrame, "Imię musi zawierać jedynie litery", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!surname.matches("[a-zA-Z]+")) {
                    JOptionPane.showMessageDialog(ownerFrame, "Nazwisko musi zawierać jedynie litery", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (registration.contains(" ")) {
                    JOptionPane.showMessageDialog(ownerFrame, "Nr. rejestracji nie może zawierać spacji", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!email.contains("@")) {
                    JOptionPane.showMessageDialog(ownerFrame, "Nieprawidłowy adres email", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!phone.matches("\\d{9}")) {
                    JOptionPane.showMessageDialog(ownerFrame, "Numer telefonu musi zawierać 9 cyfr", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date expiryDate;
                try {
                    expiryDate = dateFormat.parse(expiryDateStr);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(ownerFrame, "Nieprawidłowy format daty", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String ownerInfo = spot + "," + name + "," + surname + "," + registration + "," + email + "," + phone + "," + expiryDateStr;

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(OWNERS_FILE, true))) {
                    bw.write(ownerInfo);
                    bw.newLine();
                    JOptionPane.showMessageDialog(ownerFrame, "Właściciel dodany pomyślnie", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                    ownerFrame.dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ownerFrame, "Wystąpił błąd podczas dodawania właściciela", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
                openMainWindow();
            }
        });


        ownerPanel.add(spotLabel);
        ownerPanel.add(spotText);
        ownerPanel.add(nameLabel);
        ownerPanel.add(nameText);
        ownerPanel.add(surnameLabel);
        ownerPanel.add(surnameText);
        ownerPanel.add(registrationLabel);
        ownerPanel.add(registrationText);
        ownerPanel.add(emailLabel);
        ownerPanel.add(emailText);
        ownerPanel.add(phoneLabel);
        ownerPanel.add(phoneText);
        ownerPanel.add(expiryDateLabel);
        ownerPanel.add(expiryDateText);
        ownerPanel.add(new JLabel());
        ownerPanel.add(addButton);

        ownerFrame.add(ownerPanel, BorderLayout.CENTER);

        ownerFrame.setVisible(true);
    }

    private void displaySpots() {
        JFrame spotsFrame = new JFrame("Miejsca parkingowe");
        spotsFrame.setSize(600, 400);
        spotsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        spotsFrame.setLayout(new BorderLayout());
        spotsFrame.setLocationRelativeTo(null);
        try {
            URL iconUrl = new URL("https://i.imgur.com/ghTWxfW.png");
            ImageIcon icon = new ImageIcon(iconUrl);
            spotsFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel spotsPanel = new JPanel(new GridLayout(parkingSpots / 10, 10));

        ArrayList<Integer> occupiedSpots = new ArrayList<>();
        HashMap<Integer, String[]> ownersDataMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(OWNERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                int spotNumber = Integer.parseInt(data[0]);
                occupiedSpots.add(spotNumber);
                ownersDataMap.put(spotNumber, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= parkingSpots; i++) {
            final int spotNumber = i;

            JButton spotButton = new JButton(String.valueOf(spotNumber));
            spotButton.setPreferredSize(new Dimension(50, 50));

            if (occupiedSpots.contains(spotNumber)) {
                spotButton.setBackground(Color.RED);
                spotButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String[] ownerData = ownersDataMap.get(spotNumber);
                        String message = "Właściciel miejsca " + spotNumber + ":\n" +
                                "Imię: " + ownerData[1] + "\n" +
                                "Nazwisko: " + ownerData[2] + "\n" +
                                "Rejestacja: " + ownerData[3] + "\n" +
                                "Email: " + ownerData[4] + "\n" +
                                "Nr. telefonu: " + ownerData[5] + "\n" +
                                "Data ważności: " + ownerData[6];
                        JOptionPane.showMessageDialog(spotsFrame, message, "Informacje o właścicielu", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            } else {
                spotButton.setBackground(Color.GREEN);
                spotButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        spotsFrame.dispose();
                        openOwnerAssignmentWindow(spotNumber);
                    }
                });
            }

            spotsPanel.add(spotButton);
        }

        spotsFrame.add(spotsPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Powrót");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spotsFrame.dispose();
                mainFrame.setVisible(true);
            }
        });
        spotsFrame.add(backButton, BorderLayout.SOUTH);

        spotsFrame.setVisible(true);
    }


    private void displayOldOwners(){
        JFrame displayFrame = new JFrame("Lista poprzednich właścicieli");
        displayFrame.setSize(800, 600);
        displayFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        displayFrame.setLayout(new BorderLayout());
        displayFrame.setLocationRelativeTo(null);
        try {
            URL iconUrl = new URL("https://i.imgur.com/ghTWxfW.png");
            ImageIcon icon = new ImageIcon(iconUrl);
            displayFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        model.addColumn("Numer miejsca");
        model.addColumn("Imię");
        model.addColumn("Nazwisko");
        model.addColumn("Rejestacja");
        model.addColumn("Email");
        model.addColumn("Nr. telefonu");

        TableColumn expiryDateColumn = table.getColumnModel().getColumn(5);

        ArrayList<String[]> ownersDataList = new ArrayList<>();

        displayFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                openMainWindow();
            }
        });

        try (BufferedReader br = new BufferedReader(new FileReader(OLD_OWNERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                ownersDataList.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(ownersDataList, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int spot1 = Integer.parseInt(o1[0]);
                int spot2 = Integer.parseInt(o2[0]);
                return Integer.compare(spot1, spot2);
            }
        });

        for (String[] ownerData : ownersDataList) {
            model.addRow(ownerData);
        }

        displayFrame.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Powrót");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayFrame.setVisible(false);
                mainFrame.setVisible(true);
            }
        });


        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(40, 40, 40));
        buttonPanel.add(backButton);

        displayFrame.add(buttonPanel, BorderLayout.SOUTH);

        displayFrame.setVisible(true);
    }



    private void displayOwners() {
        JFrame displayFrame = new JFrame("Lista właścicieli");
        displayFrame.setSize(800, 600);
        displayFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        displayFrame.setLayout(new BorderLayout());
        displayFrame.setLocationRelativeTo(null);
        try {
            URL iconUrl = new URL("https://i.imgur.com/ghTWxfW.png");
            ImageIcon icon = new ImageIcon(iconUrl);
            displayFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);


        model.addColumn("Numer miejsca");
        model.addColumn("Imię");
        model.addColumn("Nazwisko");
        model.addColumn("Rejestacja");
        model.addColumn("Email");
        model.addColumn("Nr. telefonu");
        model.addColumn("Data ważności");

        TableColumn expiryDateColumn = table.getColumnModel().getColumn(6);
        expiryDateColumn.setCellRenderer(new DateCellRenderer());

        ArrayList<String[]> ownersDataList = new ArrayList<>();

        displayFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                openMainWindow();
            }
        });

        try (BufferedReader br = new BufferedReader(new FileReader(OWNERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                ownersDataList.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(ownersDataList, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int spot1 = Integer.parseInt(o1[0]);
                int spot2 = Integer.parseInt(o2[0]);
                return Integer.compare(spot1, spot2);
            }
        });
        for (String[] ownerData : ownersDataList) {
            model.addRow(ownerData);
        }

        displayFrame.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Powrót");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayFrame.setVisible(false);
                mainFrame.setVisible(true);
            }
        });

        JButton removeOwnerButton = new JButton("Usuń właściciela");
        removeOwnerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(displayFrame, "Wybierz właściciela do usunięcia", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else {
                    int confirmation = JOptionPane.showConfirmDialog(displayFrame, "Czy na pewno chcesz usunąć tego właściciela?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
                    if (confirmation == JOptionPane.YES_OPTION) {
                        String[] removedOwner = new String[7];
                        for (int i = 0; i < 7; i++) {
                            removedOwner[i] = (String) model.getValueAt(selectedRow, i);
                        }
                        model.removeRow(selectedRow);
                        saveRemovedOwnerData(removedOwner);
                        removeOwnerDataFromOwnersFile(removedOwner);
                    }
                }
            }
        });

        JButton editButton = new JButton("Edytuj właściciela");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(displayFrame, "Wybierz właściciela do edycji", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else {
                    String spot = (String) model.getValueAt(selectedRow, 0);
                    String name = (String) model.getValueAt(selectedRow, 1);
                    String surname = (String) model.getValueAt(selectedRow, 2);
                    String registration = (String) model.getValueAt(selectedRow, 3);
                    String email = (String) model.getValueAt(selectedRow, 4);
                    String phone = (String) model.getValueAt(selectedRow, 5);
                    String expiryDate = (String) model.getValueAt(selectedRow, 6);
                    openEditOwnerWindow(displayFrame, table, selectedRow, spot, name, surname, registration, email, phone, expiryDate);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(40, 40, 40));
        buttonPanel.add(backButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeOwnerButton);

        displayFrame.add(buttonPanel, BorderLayout.SOUTH);

        displayFrame.setVisible(true);
    }




    private void openEditOwnerWindow(JFrame parent, JTable table, int selectedRow, String spot, String name, String surname, String registration, String email, String phone, String expiryDate) {
        JFrame editFrame = new JFrame("Edytuj właściciela");
        editFrame.setSize(600, 400);
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editFrame.setLayout(new BorderLayout());
        editFrame.setLocationRelativeTo(parent);
        try {
            URL iconUrl = new URL("https://i.imgur.com/ghTWxfW.png");
            ImageIcon icon = new ImageIcon(iconUrl);
            editFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel editPanel = new JPanel(new GridLayout(8, 2, 5, 5));

        JLabel spotLabel = new JLabel("Numer miejsca:");
        JTextField spotText = new JTextField(spot);
        spotText.setEditable(false);
        spotLabel.setForeground(Color.WHITE);
        spotLabel.setFont(spotLabel.getFont().deriveFont(Font.BOLD));


        JLabel nameLabel = new JLabel("Imię:");
        JTextField nameText = new JTextField(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));

        JLabel surnameLabel = new JLabel("Nazwisko:");
        JTextField surnameText = new JTextField(surname);
        surnameLabel.setForeground(Color.WHITE);
        surnameLabel.setFont(surnameLabel.getFont().deriveFont(Font.BOLD));

        JLabel registrationLabel = new JLabel("Rejestracja:");
        JTextField registrationText = new JTextField(registration);
        registrationLabel.setForeground(Color.WHITE);
        registrationLabel.setFont(registrationLabel.getFont().deriveFont(Font.BOLD));

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailText = new JTextField(email);
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(emailLabel.getFont().deriveFont(Font.BOLD));

        JLabel phoneLabel = new JLabel("Nr. telefonu:");
        JTextField phoneText = new JTextField(phone);
        phoneLabel.setForeground(Color.WHITE);
        phoneLabel.setFont(phoneLabel.getFont().deriveFont(Font.BOLD));

        JLabel expiryDateLabel = new JLabel("Data ważności (RRRR-MM-DD):");
        JTextField expiryDateText = new JTextField(expiryDate);
        expiryDateLabel.setForeground(Color.WHITE);
        expiryDateLabel.setFont(expiryDateText.getFont().deriveFont(Font.BOLD));

        JButton saveButton = new JButton("Zapisz zmiany");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = nameText.getText();
                String newSurname = surnameText.getText();
                String newRegistration = registrationText.getText();
                String newEmail = emailText.getText();
                String newPhone = phoneText.getText();
                String newExpiryDate = expiryDateText.getText();


                table.setValueAt(newName, selectedRow, 1);
                table.setValueAt(newSurname, selectedRow, 2);
                table.setValueAt(newRegistration,selectedRow, 3);
                table.setValueAt(newEmail, selectedRow, 4);
                table.setValueAt(newPhone, selectedRow, 5);
                table.setValueAt(newExpiryDate, selectedRow, 6);


                saveEditedOwnerData(selectedRow, newName, newSurname, newRegistration , newEmail, newPhone, newExpiryDate);
                editFrame.dispose();
            }
        });

        editPanel.add(spotLabel);
        editPanel.add(spotText);
        editPanel.add(nameLabel);
        editPanel.add(nameText);
        editPanel.add(surnameLabel);
        editPanel.add(surnameText);
        editPanel.add(registrationLabel);
        editPanel.add(registrationText);
        editPanel.add(emailLabel);
        editPanel.add(emailText);
        editPanel.add(phoneLabel);
        editPanel.add(phoneText);
        editPanel.add(expiryDateLabel);
        editPanel.add(expiryDateText);
        editPanel.add(new JLabel());
        editPanel.add(saveButton);

        editFrame.add(editPanel, BorderLayout.CENTER);

        editFrame.setVisible(true);
    }

    private void saveEditedOwnerData(int selectedRow, String newName, String newSurname, String newregistration, String newEmail, String newPhone, String newExpiryDate) {
        File file = new File(OWNERS_FILE);
        File tempFile = new File("temp.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                if (row == selectedRow) {
                    String[] data = line.split(",");
                    String spot = data[0];
                    String[] newData = {spot, newName, newSurname, newregistration, newEmail, newPhone, newExpiryDate};
                    bw.write(String.join(",", newData));
                } else {
                    bw.write(line);
                }
                bw.newLine();
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!tempFile.renameTo(file)) {
            try {
                Files.copy(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(tempFile.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Nie udało się zapisać zmian w pliku właściciele.");
            }
        }
    }

    private void saveRemovedOwnerData(String[] removedOwner) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OLD_OWNERS_FILE, true))) {
            for (int i = 0; i < removedOwner.length; i++) {
                bw.write(removedOwner[i]);
                if (i < removedOwner.length - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        removeOwnerDataFromOwnersFile(removedOwner);
    }

    private boolean isSpotOccupied(int spotNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(OWNERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                int occupiedSpot = Integer.parseInt(data[0]);
                if (occupiedSpot == spotNumber) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void removeOwnerDataFromOwnersFile(String[] removedOwner) {
        File file = new File(OWNERS_FILE);
        File tempFile = new File("temp.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = br.readLine()) != null) {
                boolean isOwnerToRemove = true;
                String[] data = line.split(",");
                for (int i = 0; i < removedOwner.length; i++) {
                    if (!data[i].equals(removedOwner[i])) {
                        isOwnerToRemove = false;
                        break;
                    }
                }
                if (!isOwnerToRemove) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!tempFile.renameTo(file)) {
            try {
                Files.copy(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(tempFile.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Nie udało się zapisać zmian w pliku właściciele.");
            }
        }
    }




    private void loadParkingSpots() {
        File file = new File(SPOTS_FILE);
        try {
            if (!file.exists()) {
                parkingSpots = 0;
                file.createNewFile();
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line = br.readLine();
                    if (line != null) {
                        parkingSpots = Integer.parseInt(line);
                    } else {
                        parkingSpots = 0;
                    }
                }
            }
            if (parkingSpots == 0) {
                askForParkingSpots();
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void askForParkingSpots() {
        String spotsStr = JOptionPane.showInputDialog(mainFrame, "Podaj liczbę miejsc parkingowych:", "Liczba miejsc parkingowych", JOptionPane.PLAIN_MESSAGE);
        try {
            parkingSpots = Integer.parseInt(spotsStr);
            saveParkingSpots();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainFrame, "Nieprawidłowa liczba miejsc", "Błąd", JOptionPane.ERROR_MESSAGE);
            askForParkingSpots();
        }
    }


    private void saveParkingSpots() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SPOTS_FILE))) {
            bw.write(String.valueOf(parkingSpots));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ParkingManager();
    }

    private static class DateCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private Date today = new Date();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof String) {
                try {
                    Date expiryDate = dateFormat.parse((String) value);
                    if (expiryDate.before(today)) {
                        c.setBackground(Color.RED);
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return c;
        }
    }
}
