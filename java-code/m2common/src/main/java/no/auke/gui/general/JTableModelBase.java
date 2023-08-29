package no.auke.gui.general;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class JTableModelBase extends AbstractTableModel{

	private static final long serialVersionUID = 8099045230672246494L;
	
	public int NEWINSERT_HIDDEN_INDEX = -1;
    public int DELETE_INDEX=-1;
    
    protected String[] columnNames;
    protected Vector<Object[]> dataVector;
    protected Object[] sampleEmptyRow;
    protected Vector<Integer> editableColumns;
    public boolean canDelete = false;
    public boolean canAutoInsert = false;
    protected JTable table; 
    
    boolean tableDirty = false;
    public boolean isDirty(){return tableDirty;}
   
    
    public Vector<Object[]> getTableData()
    {
        return dataVector;
    }
    
    public List<Object[]> getCloneData()
    {
        List<Object[]> objs = new ArrayList<Object[]>();
        for(int index=0; index < dataVector.size(); index++)
        {
            if(!isEmpty(index))
            {
                Object[] cloneMe = dataVector.get(index).clone();
                objs.add(cloneMe);
            }
        }
        return objs;
    }
    
    
    public void setTableData(Vector<Object[]> dataSource)
    {
        dataVector = dataSource;
        fireTableDataChanged();
        tableDirty = false;
    }
  
    
    
    public JTableModelBase(JTable table, String[] columnNames, Object[] sampleEmptyRow, int[] editableColumnIndexes, boolean canAutoInsert, boolean canDelete) throws Throwable
    {
        this(table, new Vector<Object[]>(), columnNames, sampleEmptyRow, editableColumnIndexes,  canAutoInsert, canDelete);
    }
    public JTableModelBase(JTable table, Vector<Object[]> data, String[] columnNames, Object[] sampleEmptyRow, int[] editableColumnIndexes,  boolean canAutoInsert, boolean canDelete) throws Throwable
    {
        this.sampleEmptyRow = sampleEmptyRow;
        this.columnNames = columnNames;
        if(this.sampleEmptyRow.length != columnNames.length)
        {
            throw new Exception("Invalid input");
        }
        this.table = table;
        this.canAutoInsert = canAutoInsert;
        this.canDelete = canDelete;
        
        if(canAutoInsert && canDelete)
        {
            this.NEWINSERT_HIDDEN_INDEX = this.columnNames.length;
            this.DELETE_INDEX = this.NEWINSERT_HIDDEN_INDEX +1;
        }
        else if(canDelete)
        {
            this.DELETE_INDEX = this.columnNames.length;
        }
        else if(canAutoInsert)
        {
            this.NEWINSERT_HIDDEN_INDEX = this.columnNames.length;
        }
        this.dataVector = new Vector<Object[]>();
        
        this.editableColumns = new Vector<Integer>();
        for(int i:editableColumnIndexes)
        {
            if(i < columnNames.length) this.editableColumns.add(i);
        }
        initComponent(table);
    }
    
    
    
   private void initComponent(JTable table) {
           addTableModelListener(new InteractiveTableModelListener());
           table.setModel(this);
           table.setSurrendersFocusOnKeystroke(true);
           if(canAutoInsert)
           {
               if (!hasEmptyRow()) {
                   addEmptyRow();
               }
               TableColumn insertHiddenCol = table.getColumnModel().getColumn(NEWINSERT_HIDDEN_INDEX);
               insertHiddenCol.setMinWidth(2);
               insertHiddenCol.setPreferredWidth(2);
               insertHiddenCol.setMaxWidth(2);
               insertHiddenCol.setCellRenderer(new InteractiveRenderer(NEWINSERT_HIDDEN_INDEX));
           }
           if(canDelete)
           {
               
               TableColumn deleteHiddenCol = table.getColumnModel().getColumn(DELETE_INDEX);
               deleteHiddenCol.setMinWidth(14);
               deleteHiddenCol.setPreferredWidth(14);
               deleteHiddenCol.setMaxWidth(14);
               deleteHiddenCol.setCellRenderer(new DeleteEditorRenderer(DELETE_INDEX));
               deleteHiddenCol.setCellEditor(new DeleteEditorRenderer(DELETE_INDEX));
           }
           
           

          
           
   }
   
   public void setCellEditorRenderer(int columnIndex, TableCellRenderer renderer, TableCellEditor editor)
   {
       TableColumn col = table.getColumnModel().getColumn(columnIndex);
       if(renderer!=null)
       col.setCellRenderer(renderer);
       if(editor !=null)
       col.setCellEditor(editor);
   }
   
   

   public class DeleteEditorRenderer extends AbstractCellEditor implements TableCellEditor, ActionListener, TableCellRenderer {

	private static final long serialVersionUID = 6629799972255048798L;

	JButton button;
       
       int currentRow;
       int currentCol;
       protected static final String DELETE = "delete";
       protected int interactiveColumn;
       
        public DeleteEditorRenderer(int interactiveColumn) {
            
           this.interactiveColumn = interactiveColumn;
           button = new JButton();
           button.setActionCommand(DELETE);
           button.setIcon(new ImageIcon(JTableModelBase.class.getResource("/javax/swing/plaf/metal/icons/ocean/close.gif")));
           button.setPreferredSize(new Dimension(12, 12));
           button.addActionListener(this);
           button.setBorderPainted(false);
       }
        
       
       public void actionPerformed(ActionEvent e) {
           if (DELETE.equals(e.getActionCommand())) {
               
               //DELETE
               dataVector.remove(currentRow);
               fireTableRowsDeleted(currentRow, currentRow);
               fireTableDataChanged();
               
               
           }
       }
       
       public Component getTableCellRendererComponent(
               JTable table, Object color,
               boolean isSelected, boolean hasFocus,
               int row, int column) {
           return button;
       }


       @Override
       public Object getCellEditorValue() {
           
           return new Object();
       }

       //Implement the one method defined by TableCellEditor.
       public Component getTableCellEditorComponent(JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    int row,
                                                    int column) {
           currentRow = row;
           currentCol = column;
           return button;
       }
   }

   class InteractiveRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 6794142850166117035L;

	protected int interactiveColumn;

       public InteractiveRenderer(int interactiveColumn) {
           this.interactiveColumn = interactiveColumn;
       }

       public Component getTableCellRendererComponent(JTable table,
          Object value, boolean isSelected, boolean hasFocus, int row,
          int column)
       {
           Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
           if (column == interactiveColumn && hasFocus) {
               if ((getRowCount() - 1) == row && !hasEmptyRow())
               {
                  addEmptyRow();
               }

               highlightLastRow(row);
           }

           return c;
       }
   }
   
   class InteractiveTableModelListener implements TableModelListener {
       public void tableChanged(TableModelEvent evt) {
           if (evt.getType() == TableModelEvent.UPDATE) {
               
               if(canAutoInsert)
               {
                   int column = evt.getColumn();
                   int row = evt.getFirstRow();
              
                   System.out.println("row: " + row + " column: " + column);
                   table.setColumnSelectionInterval(column + 1, column + 1);
                   if(row >0) table.setRowSelectionInterval(row, row);
                   
               }   
               
           }
           
           if(evt.getType() == TableModelEvent.DELETE)
           {
                int row = evt.getFirstRow();
                if(row >0) table.setRowSelectionInterval(0, 0);
                    
                if(canAutoInsert)
                {
                   if (!hasEmptyRow()) {
                       addEmptyRow();
                   }
                }
                
               
           }
           tableDirty = true;
           
           
       }
   }
   
   public void highlightLastRow(int row) {
       int lastrow = getRowCount();
       if (row == lastrow - 1) {
           table.setRowSelectionInterval(lastrow - 1, lastrow - 1);
       } else {
           table.setRowSelectionInterval(row + 1, row + 1);
       }
       table.setColumnSelectionInterval(0, 0);
   }

   public String getColumnName(int column) {
        return column < columnNames.length? columnNames[column] : "";
    }

    
   @Override
   public int getColumnCount() {
       int num = columnNames.length ;
       if(canAutoInsert) num++;
       if(canDelete) num++;
       return num;
   }

   @Override
   public int getRowCount() {
       return dataVector.size();
   }

   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
        Object[] record = (Object[])dataVector.get(rowIndex);
        if(columnIndex < columnNames.length)
        {
            return record[columnIndex];
        }
        else return new Object();
       
   }
   
   public boolean isCellEditable(int row, int column) {
        if (column == NEWINSERT_HIDDEN_INDEX) return false;
        if(column == DELETE_INDEX) return true;
        else
        {
            if(editableColumns.contains(column)) return true;
            return false;
        }
    }
    
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Class getColumnClass(int column) {
        if(column < sampleEmptyRow.length) return sampleEmptyRow[column].getClass();
        else return String.class;
    }

   public void setValueAt(Object value, int row, int column) {
       if(column != DELETE_INDEX && column != NEWINSERT_HIDDEN_INDEX)
       {
           Object[] data = (Object[]) dataVector.get(row);
           if(column < columnNames.length)
           {
               data[column] = value;
               fireTableCellUpdated(row, column);
           }
       }
    }
    
   public boolean isEmpty(int index)
   {
       Object[] data = (Object[] )dataVector.get(index);
       for(int i =0; i < data.length; i++)
       {
           if(!data[i].equals(sampleEmptyRow[i]))
           {
               return false;
           }
       }
       return true;
   }
   public boolean hasEmptyRow() {
        if (dataVector.size() == 0) return false;
        return isEmpty(dataVector.size() - 1);
    }
    
   public void addEmptyRow() {
        Object[] obj = sampleEmptyRow.clone();
        dataVector.add(obj);
        fireTableRowsInserted(
           dataVector.size() - 1,
           dataVector.size() - 1);
        table.scrollRectToVisible(table.getCellRect(dataVector.size() - 1, 0, true));
    }
    
   public void clearAllData()
   {
       //int lastIndex = dataVector.size() -1; 
       dataVector.clear();
       //fireTableRowsDeleted(0, lastIndex);
       fireTableDataChanged();
   }
}
