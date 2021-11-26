
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import andius.objects.Conversations.Label;
import javax.swing.DefaultCellEditor;
import andius.objects.Conversations.Topic;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

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
            return c.getName() + " - " + " - " + c.getDescription();
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
                    t.setQuery(s);
                    break;
                case 1:
                    t.setPhrase(s);
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
                    t.setId(s);
                    break;
                case 1:
                    t.setQuery(s);
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
                    return t.getId();
                case 1:
                    return t.getQuery();
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
            Collections.sort(this.convs.getConversations());

            Set<String> maps = new TreeSet<>();

            Iterator<Conversation> iter = this.convs.getConversations().iterator();
            while (iter.hasNext()) {
                Conversation c = iter.next();
                if (c.getMap() == null) {
                    c.setMap("UNDEFINED");
                }
                if (c.getName() == null || c.getName().length() < 1) {
                    iter.remove();
                }
                maps.add(c.getMap());
            }

            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Map");
            for (String m : maps) {
                DefaultMutableTreeNode mapNode = new DefaultMutableTreeNode(m.toString());
                Iterator<Conversation> it = this.convs.getConversations().iterator();
                while (it.hasNext()) {
                    Conversation c = it.next();
                    if (m.equals(c.getMap())) {
                        ConversationNode convNode = new ConversationNode(c);
                        mapNode.add(convNode);
                    }
                }
                root.add(mapNode);
            }
            this.convTree.setModel(new DefaultTreeModel(root));
            this.convTree.setRootVisible(false);

            this.tm = new TopicTableItemModel();
            tm.setTopics(this.convs.getConversations().get(0).getTopics());
            this.topicTable.setModel(tm);

            this.lm = new LabelTableItemModel();
            lm.setLabels(this.convs.getConversations().get(0).getLabels());
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
                            DialogTool.this.tm.setTopics(c.getTopics());
                            DialogTool.this.tm.fireTableDataChanged();

                            DialogTool.this.lm.setLabels(c.getLabels());
                            DialogTool.this.lm.fireTableDataChanged();

                            if (c.getLabels().size() > 0 && c.getLabels().get(0).getTopics().size() > 0) {
                                DialogTool.this.ltm.setTopics(c.getLabels().get(0).getTopics());
                                DialogTool.this.ltm.fireTableDataChanged();
                            }

                            DialogTool.this.jTextFieldName.setText(c.getName());
                            DialogTool.this.jTextFieldDesc.setText(c.getDescription());
                        }
                    }
                }
            });

            DialogTool.this.jTextFieldName.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) DialogTool.this.convTree.getLastSelectedPathComponent();
                    if (n instanceof ConversationNode) {
                        ConversationNode cn = (ConversationNode) n;
                        Conversation c = cn.getUserObject();
                        if (c != null) {
                            c.setName(DialogTool.this.jTextFieldName.getText());
                            DefaultTreeModel m = (DefaultTreeModel) DialogTool.this.convTree.getModel();
                            m.nodeChanged(n);
                        }
                    }
                }
            });

            DialogTool.this.jTextFieldDesc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) DialogTool.this.convTree.getLastSelectedPathComponent();
                    if (n instanceof ConversationNode) {
                        ConversationNode cn = (ConversationNode) n;
                        Conversation c = cn.getUserObject();
                        if (c != null) {
                            c.setDescription(DialogTool.this.jTextFieldDesc.getText());
                            DefaultTreeModel m = (DefaultTreeModel) DialogTool.this.convTree.getModel();
                            m.nodeChanged(n);
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
        addPersonBtn = new javax.swing.JButton();
        removePersonBtn = new javax.swing.JButton();
        mapConversationsPane = new javax.swing.JScrollPane();
        convTree = new javax.swing.JTree();
        jTextFieldName = new javax.swing.JTextField();
        jTextFieldDesc = new javax.swing.JTextField();
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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(topicPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelPane, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelTopicsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mapConversationsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 611, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addPersonBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removePersonBtn)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldDesc)
                            .addComponent(jTextFieldName))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mapConversationsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addPersonBtn)
                    .addComponent(removePersonBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labelTopicsPane)
                    .addComponent(topicPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                    .addComponent(labelPane, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveBtn)
                    .addComponent(removeTopicBtn)
                    .addComponent(addTopicBtn))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addPersonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPersonBtnActionPerformed
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) this.convTree.getLastSelectedPathComponent();
        if (n != null) {
            DefaultTreeModel model = (DefaultTreeModel) DialogTool.this.convTree.getModel();

            Conversation conv = new Conversation();
            conv.getTopics().add(new Topic("name", ""));
            conv.getTopics().add(new Topic("job", ""));
            conv.getTopics().add(new Topic("health", ""));
            conv.getTopics().add(new Topic("look", ""));
            conv.getTopics().add(new Topic("give", ""));

            if (n instanceof ConversationNode) {
                DefaultMutableTreeNode mapNode = (DefaultMutableTreeNode) n.getParent();
                ConversationNode convNode = new ConversationNode(conv);
                model.insertNodeInto(convNode, mapNode, mapNode.getChildCount());
            } else {
                ConversationNode convNode = new ConversationNode(conv);
                model.insertNodeInto(convNode, n, n.getChildCount());
            }
        }
    }//GEN-LAST:event_addPersonBtnActionPerformed

    private void removePersonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePersonBtnActionPerformed
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) this.convTree.getLastSelectedPathComponent();
        if (n != null) {
            DefaultTreeModel model = (DefaultTreeModel) DialogTool.this.convTree.getModel();
            if (n instanceof ConversationNode) {
                model.removeNodeFromParent(n);
            } else {
                Enumeration iter = n.children();
                while (iter.hasMoreElements()) {
                    DefaultMutableTreeNode ch = (DefaultMutableTreeNode) iter.nextElement();
                    if (ch instanceof ConversationNode) {
                        model.removeNodeFromParent(ch);
                    }
                }
            }
        }
    }//GEN-LAST:event_removePersonBtnActionPerformed

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
            JAXBContext context = JAXBContext.newInstance(Conversations.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this.convs, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveBtnActionPerformed

    private void labelTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelTableMouseClicked
        int row = this.labelTable.getSelectedRow();
        if (row >= 0) {
            this.ltm.setTopics(this.lm.getLabels().get(row).getTopics());
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
    private javax.swing.JButton addPersonBtn;
    private javax.swing.JButton addTopicBtn;
    private javax.swing.JTree convTree;
    private javax.swing.JTextField jTextFieldDesc;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JScrollPane labelPane;
    private javax.swing.JTable labelTable;
    private javax.swing.JScrollPane labelTopicsPane;
    private javax.swing.JTable labelTopicsTable;
    private javax.swing.JScrollPane mapConversationsPane;
    private javax.swing.JButton removePersonBtn;
    private javax.swing.JButton removeTopicBtn;
    private javax.swing.JButton saveBtn;
    private javax.swing.JScrollPane topicPanel;
    private javax.swing.JTable topicTable;
    // End of variables declaration//GEN-END:variables
}
