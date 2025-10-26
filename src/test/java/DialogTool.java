
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import andius.objects.Conversations.Label;
import andius.objects.Conversations.Topic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Color;
import java.awt.Component;
import java.io.FileOutputStream;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DialogTool extends javax.swing.JFrame {

    public class ConversationNode extends DefaultMutableTreeNode {

        public ConversationNode(Conversation conv) {
            super(conv);
        }

        @Override
        public Conversation getUserObject() {
            return (Conversation) super.getUserObject();
        }

        @Override
        public String toString() {
            Conversation c = getUserObject();
            return c.name + " - " + " - " + c.description;
        }

    }

    public class TopicTableItemModel extends AbstractTableModel {

        private List<Topic> topics;

        private void setTopics(List<Topic> topics) {
            this.topics = topics;
        }

        private List<Topic> getTopics() {
            return this.topics;
        }

        @Override
        public int getRowCount() {
            return this.topics != null ? this.topics.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return 2;
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
            }
            return "";
        }

        @Override
        public void setValueAt(java.lang.Object v, int rowIndex, int columnIndex) {

            String s = (String) v;

            Topic t = this.topics.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    t.query = s;
                    break;
                case 1:
                    t.phrase = s;
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
                    return t.query;
                case 1:
                    return t.phrase;
            }

            return null;
        }

    }

    public class LabelTableItemModel extends AbstractTableModel {

        private List<Label> labels;

        private void setLabels(List<Label> topics) {
            this.labels = topics;
        }

        private List<Label> getLabels() {
            return this.labels;
        }

        @Override
        public int getRowCount() {
            return this.labels != null ? this.labels.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "ID";
                case 1:
                    return "QUERY";

            }
            return "";
        }

        @Override
        public void setValueAt(java.lang.Object v, int rowIndex, int columnIndex) {

            String s = (String) v;

            Label t = this.labels.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    t.id = s;
                    break;
                case 1:
                    t.query = s;
                    break;
            }

        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public Label getLabelAt(int row) {
            return this.labels.get(row);
        }

        @Override
        public java.lang.Object getValueAt(int rowIndex, int columnIndex) {

            Label t = this.labels.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return t.id;
                case 1:
                    return t.query;
            }

            return null;
        }

    }

    public class WordWrapCellRenderer extends JTextArea implements TableCellRenderer {

        public WordWrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
            if (table.getRowHeight(row) != getPreferredSize().height) {
                table.setRowHeight(row, getPreferredSize().height);
            }
            return this;
        }
    }

    private Conversations convs = null;
    private TopicTableItemModel tm = null;
    private LabelTableItemModel lm = null;
    private TopicTableItemModel ltm = null;

    public DialogTool() {
        initComponents();

        try {
            this.convs = Conversations.init();

            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Map");

            for (String map : convs.maps.keySet()) {
                DefaultMutableTreeNode mapNode = new DefaultMutableTreeNode(map);
                for (Conversation c : convs.maps.get(map)) {
                    ConversationNode convNode = new ConversationNode(c);
                    mapNode.add(convNode);
                }
                root.add(mapNode);
            }

            this.convTree.setModel(new DefaultTreeModel(root));
            this.convTree.setRootVisible(false);

            this.tm = new TopicTableItemModel();
            this.topicTable.setModel(tm);

            this.lm = new LabelTableItemModel();
            this.labelTable.setModel(lm);

            this.ltm = new TopicTableItemModel();
            this.labelTopicsTable.setModel(ltm);

            this.convTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) convTree.getLastSelectedPathComponent();
                    if (n instanceof ConversationNode) {
                        ConversationNode cn = (ConversationNode) n;
                        Conversation c = cn.getUserObject();
                        if (c != null) {
                            DialogTool.this.tm.setTopics(c.topics);
                            DialogTool.this.tm.fireTableDataChanged();

                            DialogTool.this.lm.setLabels(c.labels);
                            DialogTool.this.lm.fireTableDataChanged();

                            if (c.labels.size() > 0 && c.labels.get(0).topics.size() > 0) {
                                DialogTool.this.ltm.setTopics(c.labels.get(0).topics);
                                DialogTool.this.ltm.fireTableDataChanged();
                            }

                        }
                    }
                }
            });

            this.topicTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            this.topicTable.getColumnModel().getColumn(0).setMaxWidth(800);
            this.topicTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            this.labelTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            this.labelTable.getColumnModel().getColumn(0).setMaxWidth(100);
            this.labelTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            this.labelTopicsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            this.labelTopicsTable.getColumnModel().getColumn(0).setMaxWidth(100);
            this.labelTopicsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            UIManager.put("Table.alternateRowColor", new Color(242, 242, 242));

            for (int x = 0; x < 2; x++) {
                this.topicTable.getColumnModel().getColumn(x).setCellRenderer(new WordWrapCellRenderer());
            }
            for (int x = 0; x < 2; x++) {
                this.labelTable.getColumnModel().getColumn(x).setCellRenderer(new WordWrapCellRenderer());
            }
            for (int x = 0; x < 2; x++) {
                this.labelTopicsTable.getColumnModel().getColumn(x).setCellRenderer(new WordWrapCellRenderer());
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
        topicPanel = new javax.swing.JScrollPane();
        topicTable = new javax.swing.JTable();
        removeTopicBtn = new javax.swing.JButton();
        addTopicBtn = new javax.swing.JButton();
        mapConversationsPane = new javax.swing.JScrollPane();
        convTree = new javax.swing.JTree();
        labelPane = new javax.swing.JScrollPane();
        labelTable = new javax.swing.JTable();
        labelTopicsPane = new javax.swing.JScrollPane();
        labelTopicsTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        saveBtn.setText("SAVE");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        topicTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        topicTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "QUERY", "PHRASE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        topicTable.setRowHeight(28);
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

        convTree.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Map");
        convTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        mapConversationsPane.setViewportView(convTree);

        labelTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "QUERY"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        labelTable.setRowHeight(28);
        labelTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelTableMouseClicked(evt);
            }
        });
        labelPane.setViewportView(labelTable);

        labelTopicsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "QUERY", "PHRASE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        labelTopicsPane.setViewportView(labelTopicsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mapConversationsPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(topicPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 611, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelPane, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelTopicsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mapConversationsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelPane, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                    .addComponent(topicPanel)
                    .addComponent(labelTopicsPane))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveBtn)
                    .addComponent(removeTopicBtn)
                    .addComponent(addTopicBtn))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addTopicBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTopicBtnActionPerformed
        this.tm.getTopics().add(new Topic());
        this.tm.fireTableDataChanged();
    }//GEN-LAST:event_addTopicBtnActionPerformed

    private void removeTopicBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTopicBtnActionPerformed
        int row = this.topicTable.getSelectedRow();
        if (row >= 0) {
            this.tm.getTopics().remove(row);
            this.tm.fireTableDataChanged();
        }
    }//GEN-LAST:event_removeTopicBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().create();
            String json = gson.toJson(this.convs);

            FileOutputStream fos = new FileOutputStream("conv-temp.json");
            fos.write(json.getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveBtnActionPerformed

    private void labelTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelTableMouseClicked
        int row = this.labelTable.getSelectedRow();
        if (row >= 0) {
            this.ltm.setTopics(this.lm.getLabels().get(row).topics);
            this.ltm.fireTableDataChanged();
        }
    }//GEN-LAST:event_labelTableMouseClicked

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
    private javax.swing.JButton addTopicBtn;
    private javax.swing.JTree convTree;
    private javax.swing.JScrollPane labelPane;
    private javax.swing.JTable labelTable;
    private javax.swing.JScrollPane labelTopicsPane;
    private javax.swing.JTable labelTopicsTable;
    private javax.swing.JScrollPane mapConversationsPane;
    private javax.swing.JButton removeTopicBtn;
    private javax.swing.JButton saveBtn;
    private javax.swing.JScrollPane topicPanel;
    private javax.swing.JTable topicTable;
    // End of variables declaration//GEN-END:variables
}
