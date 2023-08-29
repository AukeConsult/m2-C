package no.auke.gui.general;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.TableCellEditor;

public class JTableFileDialogEditor extends AbstractCellEditor implements
        TableCellEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
 
    JButton button;
    JFileChooser fileChooser;
    JDialog dialog;
    protected static final String EDIT = "edit";
    Object currentVal;
    Collection<Integer> affectedRows = null;

    public JTableFileDialogEditor(boolean chooseFile, boolean chooseFolder,Collection<Integer> affectedRows) {
    
        this.affectedRows = affectedRows;
        
        button = new JButton();
        button.setBorderPainted(false);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (!new File(String.valueOf(currentVal)).exists()) {

                	fileChooser.setCurrentDirectory(new File("."));
                	
                } else {
                	
                	fileChooser.setCurrentDirectory(new File(String.valueOf(currentVal)));
                }
                            
                
                int r = fileChooser.showOpenDialog(button);

                if (r == JFileChooser.OPEN_DIALOG
                        || r == JFileChooser.APPROVE_OPTION) {
                    currentVal = fileChooser.getSelectedFile().getPath().replace("\\", "/");

                }

                fireEditingStopped();

            }

        });

        // Set up the dialog that the button brings up.
        fileChooser = new JFileChooser();
        if (chooseFile && chooseFolder) {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        } else if (chooseFile) {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        } else if (chooseFolder) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
    }

    @Override
    public Object getCellEditorValue() {

        return currentVal;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
      
    	if (affectedRows == null || affectedRows.contains(row)) {
        
    		currentVal = value;
            return button;
        
    	} else {

            currentVal = value;
            final JTextField d = new JTextField(String.valueOf(currentVal));
            d.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent arg0) {
                    currentVal = d.getText();
                }
            });
            return d;

        }

    }
}