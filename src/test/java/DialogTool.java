
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
import andius.objects.Conversations.Label;
import javax.swing.DefaultCellEditor;
import andius.objects.Conversations.Topic;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
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
            return this.topics.size();
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
            return this.labels.size();
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
    
    private Conversations convs = null;
    private TopicTableItemModel tm = null;
    private LabelTableItemModel lm = null;

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
            this.jTree1.setModel(new DefaultTreeModel(root));
            this.jTree1.setRootVisible(false);

            this.tm = new TopicTableItemModel();
            tm.setTopics(this.convs.getConversations().get(0).getTopics());
            this.topicTable.setModel(tm);
            
            this.lm = new LabelTableItemModel();
            lm.setLabels(this.convs.getConversations().get(0).getLabels());
            this.labelTable.setModel(lm);

            this.jTree1.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
                    if (n instanceof ConversationNode) {
                        ConversationNode cn = (ConversationNode) n;
                        Conversation c = cn.getUserObject();
                        if (c != null) {
                            DialogTool.this.tm.setTopics(c.getTopics());
                            DialogTool.this.tm.fireTableDataChanged();
                            
                            DialogTool.this.lm.setLabels(c.getLabels());
                            DialogTool.this.lm.fireTableDataChanged();

                            DialogTool.this.jTextFieldName.setText(c.getName());
                            DialogTool.this.jTextFieldDesc.setText(c.getDescription());
                        }
                    }
                }
            });

            DialogTool.this.jTextFieldName.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) DialogTool.this.jTree1.getLastSelectedPathComponent();
                    if (n instanceof ConversationNode) {
                        ConversationNode cn = (ConversationNode) n;
                        Conversation c = cn.getUserObject();
                        if (c != null) {
                            c.setName(DialogTool.this.jTextFieldName.getText());
                            DefaultTreeModel m = (DefaultTreeModel) DialogTool.this.jTree1.getModel();
                            m.nodeChanged(n);
                        }
                    }
                }
            });

            DialogTool.this.jTextFieldDesc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) DialogTool.this.jTree1.getLastSelectedPathComponent();
                    if (n instanceof ConversationNode) {
                        ConversationNode cn = (ConversationNode) n;
                        Conversation c = cn.getUserObject();
                        if (c != null) {
                            c.setDescription(DialogTool.this.jTextFieldDesc.getText());
                            DefaultTreeModel m = (DefaultTreeModel) DialogTool.this.jTree1.getModel();
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

            UIManager.put("Table.alternateRowColor", new Color(242, 242, 242));

            JTextField textField = new JTextField();
            textField.setFont(new Font("Tahoma", 0, 12));
            textField.setBorder(new LineBorder(Color.BLACK));
            DefaultCellEditor dce = new DefaultCellEditor(textField);
            for (int x = 0; x < 2; x++) {
                this.topicTable.getColumnModel().getColumn(x).setCellEditor(dce);
            }
            for (int x = 0; x < 2; x++) {
                this.labelTable.getColumnModel().getColumn(x).setCellEditor(dce);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jTextFieldName = new javax.swing.JTextField();
        jTextFieldDesc = new javax.swing.JTextField();
        labelPane = new javax.swing.JScrollPane();
        labelTable = new javax.swing.JTable();

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

        jTree1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Map");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(jTree1);

        labelTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
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
        labelPane.setViewportView(labelTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(addTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveBtn))
                    .addComponent(topicPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1108, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addPersonBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removePersonBtn))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 611, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextFieldName)
                                    .addComponent(jTextFieldDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addPersonBtn)
                    .addComponent(removePersonBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topicPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelPane, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
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
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) this.jTree1.getLastSelectedPathComponent();
        if (n != null) {
            DefaultTreeModel model = (DefaultTreeModel) DialogTool.this.jTree1.getModel();

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
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) this.jTree1.getLastSelectedPathComponent();
        if (n != null) {
            DefaultTreeModel model = (DefaultTreeModel) DialogTool.this.jTree1.getModel();
            if (n instanceof ConversationNode) {
                DefaultMutableTreeNode mapNode = (DefaultMutableTreeNode) n.getParent();
                model.removeNodeFromParent(n);
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldDesc;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTree jTree1;
    private javax.swing.JScrollPane labelPane;
    private javax.swing.JTable labelTable;
    private javax.swing.JButton removePersonBtn;
    private javax.swing.JButton removeTopicBtn;
    private javax.swing.JButton saveBtn;
    private javax.swing.JScrollPane topicPanel;
    private javax.swing.JTable topicTable;
    // End of variables declaration//GEN-END:variables
}
