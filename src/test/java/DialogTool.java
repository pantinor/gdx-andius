
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;
import andius.Constants.Map;
import andius.objects.Conversations.Topic;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Paul
 */
public class DialogTool extends javax.swing.JFrame {

    public class ConversationTableItemModel extends AbstractTableModel {

        private Conversations convs;

        public ConversationTableItemModel(Conversations convs) {
            this.convs = convs;
        }

        @Override
        public int getRowCount() {
            return this.convs.getConversations().size();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public void setValueAt(java.lang.Object v, int rowIndex, int columnIndex) {

            String s = (String) v;

            Conversation c = this.convs.getConversations().get(rowIndex);
            switch (columnIndex) {
                case 0:
                    c.setMap(Map.valueOf(s));
                    break;
                case 1:
                    c.setName(s);
                    break;
                case 2:
                    c.setPronoun(s);
                    break;
                case 3:
                    c.setDescription(s);
                    break;
            }

        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "MAP";
                case 1:
                    return "NAME";
                case 2:
                    return "PRONOUN";
                case 3:
                    return "DESCRIPTION";
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public java.lang.Object getValueAt(int rowIndex, int columnIndex) {

            Conversation c = this.convs.getConversations().get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return c.getMap();
                case 1:
                    return c.getName();
                case 2:
                    return c.getPronoun();
                case 3:
                    return c.getDescription();
            }

            return null;
        }

    }

    public class TopicTableItemModel extends AbstractTableModel {

        private List<Topic> topics;

        private void setTopics(List<Topic> topics) {
            this.topics = topics;
        }

        @Override
        public int getRowCount() {
            return this.topics.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "QUERY";
                case 1:
                    return "PHRASE";
                case 2:
                    return "QUESTION";
                case 3:
                    return "YES RESPONSE";
                case 4:
                    return "NO RESPONSE";
            }
            return "";
        }

        @Override
        public void setValueAt(java.lang.Object v, int rowIndex, int columnIndex) {

            String s = (String) v;

            Topic t = this.topics.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    t.setQuery(s);
                    break;
                case 1:
                    t.setPhrase(s);
                    break;
                case 2:
                    t.setQuestion(s);
                    break;
                case 3:
                    t.setYesResponse(s);
                    break;
                case 4:
                    t.setNoResponse(s);
                    break;
            }

        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public Topic getTopicAt(int row) {
            return this.topics.get(row);
        }

        @Override
        public java.lang.Object getValueAt(int rowIndex, int columnIndex) {

            Topic t = this.topics.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return t.getQuery();
                case 1:
                    return t.getPhrase();
                case 2:
                    return t.getQuestion();
                case 3:
                    return t.getYesResponse();
                case 4:
                    return t.getNoResponse();
            }

            return null;
        }

    }

    private Conversations convs = null;
    private ConversationTableItemModel cm = null;
    private TopicTableItemModel tm = null;

    public DialogTool() {
        initComponents();
        try {
            this.convs = Conversations.init();
            Collections.sort(this.convs.getConversations());

            Iterator<Conversation> iter = this.convs.getConversations().iterator();
            while (iter.hasNext()) {
                Conversation c = iter.next();
                if (c.getMap() == null) {
                    c.setMap(Map.LLECHY);
                }
                if (c.getName() == null || c.getName().length() < 1) {
                    iter.remove();
                }
            }

            this.cm = new ConversationTableItemModel(this.convs);
            this.convTable.setModel(cm);

            this.tm = new TopicTableItemModel();
            tm.setTopics(this.convs.getConversations().get(0).getTopics());
            this.topicTable.setModel(tm);

            TableColumn mapCol = this.convTable.getColumnModel().getColumn(0);
            JComboBox comboBox = new JComboBox();
            for (Map m : Map.values()) {
                comboBox.addItem(m.toString());
            }
            mapCol.setCellEditor(new DefaultCellEditor(comboBox));

            this.convTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent lse) {
                    if (!lse.getValueIsAdjusting()) {
                        DefaultListSelectionModel dlsm = (DefaultListSelectionModel) lse.getSource();
                        int row = dlsm.isSelectedIndex(lse.getLastIndex()) ? lse.getLastIndex() : lse.getFirstIndex();
                        if (row >= 0) {
                            Conversation c = DialogTool.this.convs.getConversations().get(row);
                            if (c != null) {
                                DialogTool.this.tm.setTopics(c.getTopics());
                                DialogTool.this.tm.fireTableDataChanged();
                            }
                        }
                    }
                }
            });

            this.convTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            this.convTable.getColumnModel().getColumn(0).setMaxWidth(500);
            this.convTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            this.convTable.getColumnModel().getColumn(1).setMaxWidth(500);
            this.convTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            this.convTable.getColumnModel().getColumn(2).setMaxWidth(100);
            this.convTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            this.topicTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            this.topicTable.getColumnModel().getColumn(0).setMaxWidth(100);
            this.topicTable.getColumnModel().getColumn(1).setPreferredWidth(400);
            this.topicTable.getColumnModel().getColumn(1).setMaxWidth(1000);
            this.topicTable.getColumnModel().getColumn(2).setPreferredWidth(400);
            this.topicTable.getColumnModel().getColumn(2).setMaxWidth(1000);
            this.topicTable.getColumnModel().getColumn(3).setPreferredWidth(400);
            this.topicTable.getColumnModel().getColumn(3).setMaxWidth(1000);
            this.topicTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            UIManager.put("Table.alternateRowColor", Color.YELLOW);

            JTextField textField = new JTextField();
            textField.setFont(new Font("Tahoma", 0, 24));
            textField.setBorder(new LineBorder(Color.BLACK));
            DefaultCellEditor dce = new DefaultCellEditor(textField);
            for (int x = 1; x < 4; x++) {
                this.convTable.getColumnModel().getColumn(x).setCellEditor(dce);
            }
            for (int x = 0; x < 5; x++) {
                this.topicTable.getColumnModel().getColumn(x).setCellEditor(dce);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        saveBtn = new javax.swing.JButton();
        conversationPanel = new javax.swing.JScrollPane();
        convTable = new javax.swing.JTable();
        topicPanel = new javax.swing.JScrollPane();
        topicTable = new javax.swing.JTable();
        removeTopicBtn = new javax.swing.JButton();
        addTopicBtn = new javax.swing.JButton();
        addPersonBtn = new javax.swing.JButton();
        removePersonBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        saveBtn.setText("SAVE");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        convTable.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        convTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "MAP", "NAME", "PRONOUN", "DESCRIPTION"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        convTable.setRowHeight(32);
        conversationPanel.setViewportView(convTable);

        topicTable.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        topicTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "QUERY", "PHRASE", "QUESTION", "YES", "NO"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        topicTable.setRowHeight(32);
        topicPanel.setViewportView(topicTable);

        removeTopicBtn.setText("REMOVE TOPIC");
        removeTopicBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTopicBtnActionPerformed(evt);
            }
        });

        addTopicBtn.setText("ADD TOPIC");
        addTopicBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTopicBtnActionPerformed(evt);
            }
        });

        addPersonBtn.setText("ADD PERSON");
        addPersonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPersonBtnActionPerformed(evt);
            }
        });

        removePersonBtn.setText("REMOVE PERSON");
        removePersonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePersonBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveBtn))
                    .addComponent(topicPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1108, Short.MAX_VALUE)
                    .addComponent(conversationPanel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addPersonBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removePersonBtn)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conversationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addPersonBtn)
                    .addComponent(removePersonBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topicPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveBtn)
                    .addComponent(removeTopicBtn)
                    .addComponent(addTopicBtn))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addPersonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPersonBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addPersonBtnActionPerformed

    private void removePersonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePersonBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_removePersonBtnActionPerformed

    private void addTopicBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTopicBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addTopicBtnActionPerformed

    private void removeTopicBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTopicBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_removeTopicBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DialogTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DialogTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DialogTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DialogTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DialogTool().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPersonBtn;
    private javax.swing.JButton addTopicBtn;
    private javax.swing.JTable convTable;
    private javax.swing.JScrollPane conversationPanel;
    private javax.swing.JButton removePersonBtn;
    private javax.swing.JButton removeTopicBtn;
    private javax.swing.JButton saveBtn;
    private javax.swing.JScrollPane topicPanel;
    private javax.swing.JTable topicTable;
    // End of variables declaration//GEN-END:variables
}
