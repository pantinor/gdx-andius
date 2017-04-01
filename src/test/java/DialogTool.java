
import andius.objects.Conversations;
import andius.objects.Conversations.Conversation;
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
import javax.swing.tree.TreeNode;
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
            return c.getName() + " - " + c.getPronoun() + " - " + c.getDescription();
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
    private TopicTableItemModel tm = null;

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

                            DialogTool.this.jTextFieldName.setText(c.getName());
                            DialogTool.this.jTextFieldPronoun.setText(c.getPronoun());
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

            DialogTool.this.jTextFieldPronoun.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) DialogTool.this.jTree1.getLastSelectedPathComponent();
                    if (n instanceof ConversationNode) {
                        ConversationNode cn = (ConversationNode) n;
                        Conversation c = cn.getUserObject();
                        if (c != null) {
                            c.setPronoun(DialogTool.this.jTextFieldPronoun.getText());
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

            this.topicTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            this.topicTable.getColumnModel().getColumn(0).setMaxWidth(100);
            this.topicTable.getColumnModel().getColumn(1).setPreferredWidth(400);
            this.topicTable.getColumnModel().getColumn(1).setMaxWidth(1000);
            this.topicTable.getColumnModel().getColumn(2).setPreferredWidth(400);
            this.topicTable.getColumnModel().getColumn(2).setMaxWidth(1000);
            this.topicTable.getColumnModel().getColumn(3).setPreferredWidth(400);
            this.topicTable.getColumnModel().getColumn(3).setMaxWidth(1000);
            this.topicTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            UIManager.put("Table.alternateRowColor", new Color(242, 242, 242));

            JTextField textField = new JTextField();
            textField.setFont(new Font("Tahoma", 0, 16));
            textField.setBorder(new LineBorder(Color.BLACK));
            DefaultCellEditor dce = new DefaultCellEditor(textField);
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
        topicPanel = new javax.swing.JScrollPane();
        topicTable = new javax.swing.JTable();
        removeTopicBtn = new javax.swing.JButton();
        addTopicBtn = new javax.swing.JButton();
        addPersonBtn = new javax.swing.JButton();
        removePersonBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jTextFieldName = new javax.swing.JTextField();
        jTextFieldPronoun = new javax.swing.JTextField();
        jTextFieldDesc = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        saveBtn.setText("SAVE");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        topicTable.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
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

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Map");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(jTree1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(addTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeTopicBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveBtn))
                    .addComponent(topicPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1108, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(addPersonBtn)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(removePersonBtn))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 615, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jTextFieldName)
                                .addComponent(jTextFieldPronoun)
                                .addComponent(jTextFieldDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)))))
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
                        .addComponent(jTextFieldPronoun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) this.jTree1.getLastSelectedPathComponent();
        if (n != null) {
            DefaultTreeModel model = (DefaultTreeModel) DialogTool.this.jTree1.getModel();

            Conversation conv = new Conversation();
            conv.getTopics().add(new Topic("name", "", "", "", ""));
            conv.getTopics().add(new Topic("job", "", "", "", ""));
            conv.getTopics().add(new Topic("health", "", "", "", ""));
            conv.getTopics().add(new Topic("look", "", "", "", ""));
            conv.getTopics().add(new Topic("give", "", "", "", ""));

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
    private javax.swing.JTextField jTextFieldPronoun;
    private javax.swing.JTree jTree1;
    private javax.swing.JButton removePersonBtn;
    private javax.swing.JButton removeTopicBtn;
    private javax.swing.JButton saveBtn;
    private javax.swing.JScrollPane topicPanel;
    private javax.swing.JTable topicTable;
    // End of variables declaration//GEN-END:variables
}
