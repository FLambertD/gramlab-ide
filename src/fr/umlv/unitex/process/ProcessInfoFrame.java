/*
 * Unitex
 *
 * Copyright (C) 2001-2010 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */

package fr.umlv.unitex.process;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

import javax.swing.*;
import fr.umlv.unitex.*;
import fr.umlv.unitex.console.Console;
import fr.umlv.unitex.console.ConsoleEntry;

/**
 * This class describes a frame used to execute shell commands and display
 * stdout and stderr messages produced by these processes.
 * 
 * @author Sébastien Paumier
 * 
 */
public class ProcessInfoFrame extends JInternalFrame {
	Process p;

	ProcessOutputListModel stdoutModel = new ProcessOutputListModel();

	ProcessOutputListModel stderrModel = new ProcessOutputListModel();

	JList stdoutList = new JList(stdoutModel);

	JList stderrList = new JList(stderrModel);

	public static Color systemColor = new Color(0xF0, 0xCB, 0xAA);

	public static DefaultListCellRenderer myRenderer = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList l, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Couple c = (Couple) value;
			setBackground(c.isSystemMessage ? systemColor : Color.WHITE);
			setForeground(l.getForeground());
			setText(c.s);
			return this;
		}
	};

	boolean close_on_finish;

	boolean stop_if_problem;

	MultiCommands commands = null;

	ToDoAbstract DO;

	/**
	 * Creates a new <code>ProcessInfoFrame</code>
	 * 
	 * @param c
	 *            array containing the shell commands to run
	 * @param close
	 *            indicates if the frame must be closed after the completion of
	 *            all commands
	 * @param myDo
	 *            object describing actions to do after the completion of all
	 *            commands
	 */
	public ProcessInfoFrame(MultiCommands c, boolean close, ToDoAbstract myDo) {
		this(c, close, myDo, true);
	}

	/**
	 * Creates a new <code>ProcessInfoFrame</code>
	 * 
	 * @param c
	 *            array containing the shell commands to run
	 * @param close
	 *            indicates if the frame must be closed after the completion of
	 *            all commands
	 * @param myDo
	 *            object describing actions to do after the completion of all
	 *            commands
	 * @param stopIfProblem
	 *            indicates if the failure of a command must stop all commands
	 */
	public ProcessInfoFrame(MultiCommands c, boolean close, ToDoAbstract myDo,
			boolean stopIfProblem) {
		super("Working...", true, false, false);
		commands = c;
		close_on_finish = close;
		stop_if_problem = stopIfProblem;
		DO = myDo;
		JPanel top = new JPanel();
		top.setOpaque(true);
		top.setLayout(new BorderLayout());
		stdoutList.setCellRenderer(myRenderer);
		stderrList.setCellRenderer(myRenderer);
		JScrollPane scroll = new JScrollPane(stdoutList);
		stderrList.setForeground(Color.RED);
		JScrollPane errorScroll = new JScrollPane(stderrList);
		JPanel tmp = new JPanel();
		tmp.setOpaque(true);
		tmp.setLayout(new BorderLayout());
		tmp.setBorder(BorderFactory.createLoweredBevelBorder());
		tmp.add(scroll, BorderLayout.CENTER);
		JPanel tmp2 = new JPanel();
		tmp2.setOpaque(true);
		tmp2.setLayout(new BorderLayout());
		tmp2.setBorder(BorderFactory.createLoweredBevelBorder());
		tmp2.add(errorScroll, BorderLayout.CENTER);
		JSplitPane middle = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tmp, tmp2);
		middle.setDividerLocation(250);
		top.add(middle, BorderLayout.CENTER);
		Action okAction = new AbstractAction("OK") {
			public void actionPerformed(ActionEvent arg0) {
				if (p != null) {
					try {
						p.exitValue();
					} catch (IllegalThreadStateException ex) {
						return;
					}
				}
				setVisible(false);
				UnitexFrame.removeInternalFrame(ProcessInfoFrame.this);
			}
		};
		JButton ok = new JButton(okAction);
		Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent arg0) {
				if (p != null)
					p.destroy();
				setVisible(false);
				UnitexFrame.removeInternalFrame(ProcessInfoFrame.this);
			}
		};
		JButton cancel = new JButton(cancelAction);
		JPanel buttons = new JPanel(new GridLayout(1, 2));
		buttons.setOpaque(true);
		buttons.add(ok);
		buttons.add(cancel);
		top.add(buttons, BorderLayout.SOUTH);
		JLabel info = new JLabel(
				"Messages with a colored background are generated by the interface, not by the external programs.");
		info.setBackground(systemColor);
		info.setOpaque(true);
		top.add(info, BorderLayout.NORTH);
		setContentPane(top);
		pack();
		setBounds(100, 100, 600, 400);
		UnitexFrame.addInternalFrame(this,true);
		setVisible(true);
		launchBuilderCommands();
	}

	/**
	 * Creates a new <code>ProcessInfoFrame</code>
	 * 
	 * @param c
	 *            array containing the shell commands to run
	 * @param close
	 *            indicates if the frame must be closed after the completion of
	 *            all commands
	 */
	public ProcessInfoFrame(MultiCommands c, boolean close) {
		this(c, close, null);
	}

	/**
	 * Creates a new <code>ProcessInfoFrame</code>
	 * 
	 * @param c
	 *            the shell command to run
	 * @param close
	 *            indicates if the frame must be closed after the completion of
	 *            all commands
	 */
	public ProcessInfoFrame(CommandBuilder c, boolean close) {
		this(new MultiCommands(c), close, null);
	}

	/**
	 * Creates a new <code>ProcessInfoFrame</code>
	 * 
	 * @param c
	 *            the shell commands to run
	 * @param close
	 *            indicates if the frame must be closed after the completion of
	 *            all commands
	 * @param DO
	 *            object describing actions to do after the completion of all
	 *            commands
	 */
	public ProcessInfoFrame(CommandBuilder c, boolean close, ToDoAbstract DO) {
		this(new MultiCommands(c), close, DO);
	}

	private void launchBuilderCommands() {
		if (commands == null)
			return;
		new Thread() {
			public void run() {
				boolean problem = false;
				CommandBuilder command;
				for (int i = 0; (!problem) && i < commands.numberOfCommands(); i++)
					if ((command = commands.getCommand(i)) != null) {
						switch (command.getType()) {
						case CommandBuilder.MESSAGE: {
							try {
								final CommandBuilder c=command;
								SwingUtilities.invokeAndWait(new Runnable() {
									public void run() {
										stdoutModel.addElement(new Couple(
												((MessageCommand) c).getMessage(),
												true));
									}});
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							} catch (InvocationTargetException e1) {
								e1.printStackTrace();
							}
							break;
						}
						case CommandBuilder.ERROR_MESSAGE: {
                            final ConsoleEntry entry=Console.addCommand("Error message emitted by the graphical interface",true);
							try {
								final CommandBuilder c=command;
								SwingUtilities.invokeAndWait(new Runnable() {
									public void run() {
									    String message=((MessageCommand) c).getMessage();
										stderrModel.addElement(new Couple(message,true));
										entry.addErrorMessage(message);
									}});
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							} catch (InvocationTargetException e1) {
								e1.printStackTrace();
							}
							break;
						}
						case CommandBuilder.PROGRAM: {
							ConsoleEntry entry=Console.addCommand(command.getCommandLine(),false);
							final String[] comm = command.getCommandArguments();
							try {
								p = Runtime.getRuntime().exec(comm);
								new ProcessInfoThread(stdoutList, p
										.getInputStream(), false,
										ProcessInfoFrame.this,true,null).start();
								new ProcessInfoThread(stderrList, p
										.getErrorStream(), false,
										ProcessInfoFrame.this,true,entry).start();
								try {
									p.waitFor();
								} catch (java.lang.InterruptedException e) {
									if (stop_if_problem) {
										try {
											SwingUtilities.invokeAndWait(new Runnable() {
												public void run() {
													stderrModel
													.addElement(new Couple(
															"The program "
																	+ comm[0]
																	+ " has been interrupted\n",
															true));
													stderrList.ensureIndexIsVisible(stderrModel
												.	getSize() - 1);
												}});
										} catch (InterruptedException e1) {
											e1.printStackTrace();
										} catch (InvocationTargetException e1) {
											e1.printStackTrace();
										}
										problem = true;
									}
								}
							} catch (java.io.IOException e) {
								final String programName = comm[0];
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										stderrModel.addElement(new Couple(
												"Cannot launch the program "
														+ programName + "\n",
												true));
										stderrList
												.ensureIndexIsVisible(stderrModel
														.getSize() - 1);
									}
								});
								if (stop_if_problem) {
									problem = true;
								}
							}
							try {
								if (p == null) {
									if (stop_if_problem) {
										problem = true;
									}
								} else if (p.exitValue() != 0) {
									if (stop_if_problem) {
										problem = true;
									}
								}
							} catch (IllegalThreadStateException e) {
								p.destroy();
							}
							break;
						}// end of program command
						
					case CommandBuilder.METHOD: {
						Console.addCommand(command.getCommandLine(),false);
						AbstractMethodCommand cmd=(AbstractMethodCommand)command;
						if (!cmd.execute()) {
							if (stop_if_problem) {
								try {
									final CommandBuilder c=command;
									SwingUtilities.invokeAndWait(new Runnable() {
										public void run() {
											stderrModel.addElement(new Couple(
													"Command failed: "+c.getCommandLine(),
													true));
										}});
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								} catch (InvocationTargetException e1) {
									e1.printStackTrace();
								}
								problem = true;
							}
						}
						break;
					}// end of method command

						} // end of switch
					} // end of if ((command = commands.getCommand(i)) != null)
				final boolean PB=problem,CL=close_on_finish;
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							if (PB == true) {
								setTitle("ERROR");
							} else {
								setTitle("");
							}
							if (!PB && CL) {
								ProcessInfoFrame.this.setVisible(false);
								UnitexFrame.removeInternalFrame(ProcessInfoFrame.this);
							}
						}});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				if (!problem && DO != null) {
					DO.toDo();
				}
			}
		}.start();
	}

}