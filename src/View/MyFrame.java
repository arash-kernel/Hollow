package View;

import Controller.*;
import Model.Game.Knight.Knight;
import Model.Game.Room;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MyFrame extends JFrame {
	public static MyPanel onlyPanel;

	public MyFrame(Room room, Ui ui) {
		Knight knight=room.getKnight();
		onlyPanel = new MyPanel(room, ui);
		ui.setPanel(onlyPanel); 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(onlyPanel);
		this.pack();
		this.setLocationRelativeTo(null);

		
		this.setFocusable(true);
		onlyPanel.setFocusable(false);

		this.setVisible(true);
		this.requestFocusInWindow();

		this.addKeyListener(new KeyAdapter() {
			private int keysHeldCount = 0;
			private boolean interactHeld = false;

			@Override
			public void keyPressed(KeyEvent e) {
				
				Room currentRoom = onlyPanel.room;
				Knight currentKnight = currentRoom.getKnight();

				keysHeldCount++;

				
				if (SystemController.getCurrentState() != SystemController.GameState.REMAPPING) {
					if (GeneralSave.keybinds != null && GeneralSave.keybinds.get("Pause") != null) {
						if (e.getKeyCode() == GeneralSave.keybinds.get("Pause")) {
							if (SystemController.getCurrentState() == SystemController.GameState.PLAYING) {
								SystemController.setCurrentState(SystemController.GameState.PAUSED);
							} else if (SystemController.getCurrentState() == SystemController.GameState.PAUSED) {
								SystemController.setCurrentState(SystemController.GameState.PLAYING);
							}
							return;
						}
					}
				}

				
				if (SystemController.getCurrentState() != SystemController.GameState.REMAPPING) {
					if (e.getKeyCode() == KeyEvent.VK_I) {
						if (SystemController.getCurrentState() == SystemController.GameState.PLAYING) {
							SystemController.setCurrentState(SystemController.GameState.INVENTORY);
						} else if (SystemController.getCurrentState() == SystemController.GameState.INVENTORY) {
							SystemController.setCurrentState(SystemController.GameState.PLAYING);
						}
						return;
					}
				}

				
				if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS) {
					if (ui != null && ui.getSettingsUi() != null) {
						ui.getSettingsUi().handleKeyPressed(e);
					}
				}

				
				if (SystemController.getCurrentState() == SystemController.GameState.REMAPPING) {
					if (ui != null && ui.getRemappingUi() != null) {
						ui.getRemappingUi().handleKeyPressed(e);
					}
					return;
				}

				
				if (SystemController.getCurrentState() == SystemController.GameState.PLAYING) {

					
					if (currentRoom.getDialogueBox() != null) {
						boolean isInteractKey = (e.getKeyCode() == KeyEvent.VK_SPACE ||
								e.getKeyCode() == KeyEvent.VK_X ||
								e.getKeyCode() == KeyEvent.VK_ENTER);

						if (isInteractKey && !interactHeld) {
							interactHeld = true;
							currentRoom.getDialogueBox().setAdvanceTriggered(true);
						}

						currentRoom.getDialogueBox().setInputs(true, interactHeld);
						return;
					}

					
					if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) currentKnight.setRight(true);
					if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) currentKnight.setLeft(true);
					if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) currentKnight.setUp(true);
					if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) currentKnight.setDown(true);

					if (e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_J) {
						if (!currentKnight.isAttackHeld()) currentKnight.setAttackJustPressed(true);
						currentKnight.setAttackHeld(true);
					}

					if (e.getKeyCode() == KeyEvent.VK_K) {
						if (!currentKnight.isDashHeld()) currentKnight.setDashJustPressed(true);
						currentKnight.setDashHeld(true);
					}

					if (e.getKeyCode() == KeyEvent.VK_SPACE) {
						if(!currentKnight.isJumpHeld()) {
							currentKnight.setJumpJustPressed(true);
						}
						currentKnight.setJumpHeld(true);
						currentKnight.setJumpReleased(false);
					}

					if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
						currentKnight.setFocusHeld(true);
					}

					if (e.getKeyCode() == KeyEvent.VK_F) {
						if (!currentKnight.isQuickCastHeld()) currentKnight.setQuickCastJustPressed(true);
						currentKnight.setQuickCastHeld(true);
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
				Room currentRoom = onlyPanel.room;
				Knight currentKnight = currentRoom.getKnight();

				keysHeldCount = Math.max(0, keysHeldCount - 1);

				if (SystemController.getCurrentState() == SystemController.GameState.REMAPPING) {
					return;
				}

				boolean isInteractKey = (e.getKeyCode() == KeyEvent.VK_SPACE ||
						e.getKeyCode() == KeyEvent.VK_X ||
						e.getKeyCode() == KeyEvent.VK_ENTER);
				if (isInteractKey) {
					interactHeld = false;
				}

				
				if (currentRoom.getDialogueBox() != null) {
					currentRoom.getDialogueBox().setInputs(keysHeldCount > 0, false);
				}

				
				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) currentKnight.setRight(false);
				if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) currentKnight.setLeft(false);
				if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) currentKnight.setUp(false);
				if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) currentKnight.setDown(false);

				if (e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_J) currentKnight.setAttackHeld(false);
				if (e.getKeyCode() == KeyEvent.VK_K) currentKnight.setDashHeld(false);

				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					currentKnight.setJumpHeld(false);
					currentKnight.setJumpReleased(true);
				}
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) currentKnight.setFocusHeld(false);
				if (e.getKeyCode() == KeyEvent.VK_F) currentKnight.setQuickCastHeld(false);
			}
		});

		
		onlyPanel.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (SystemController.getCurrentState() == SystemController.GameState.INVENTORY) {
					ui.getInventoryUi().handleMouseClick(e.getX(), e.getY());
				} else if (SystemController.getCurrentState() == SystemController.GameState.MAIN_MENU) {
					if (ui != null && ui.getMainMenuUi() != null) {
						ui.getMainMenuUi().handleMouseClick(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.GUIDE) {
					ui.getGuideUi().handleMouseClick(e.getX(), e.getY());
				} else if (SystemController.getCurrentState() == SystemController.GameState.START_GAME) {
					if (ui != null && ui.getStartGameUi() != null) {
						ui.getStartGameUi().handleMouseClick(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS) {
					if (ui != null && ui.getSettingsUi() != null) {
						ui.getSettingsUi().handleMouseClick(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.REMAPPING) { 
					if (ui != null && ui.getRemappingUi() != null) {
						ui.getRemappingUi().handleMouseClick(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.ACHIEVEMENTS) {
					if (ui != null && ui.getAchievementsUi() != null) {
						ui.getAchievementsUi().handleMouseClick(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.PAUSED) {
					if (ui != null && ui.getPauseUi() != null) {
						ui.getPauseUi().handleMouseClick(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.VICTORY) {
				if (ui != null && ui.getVictoryUi() != null) {
					ui.getVictoryUi().handleMouseClick(e.getX(), e.getY());
				}
			}
				
				MyFrame.this.requestFocusInWindow();
			}

			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS) {
					if (ui != null && ui.getSettingsUi() != null) {
						ui.getSettingsUi().handleMousePressed(e.getX(), e.getY());
					}
				}
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS) {
					if (ui != null && ui.getSettingsUi() != null) {
						ui.getSettingsUi().handleMouseReleased(e.getX(), e.getY());
					}
				}
			}
		});

		onlyPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override
			public void mouseMoved(java.awt.event.MouseEvent e) {
				if (SystemController.getCurrentState() == SystemController.GameState.INVENTORY) {
					ui.getInventoryUi().handleMouseMove(e.getX(), e.getY());
				} else if (SystemController.getCurrentState() == SystemController.GameState.MAIN_MENU) {
					if (ui != null && ui.getMainMenuUi() != null) {
						ui.getMainMenuUi().handleMouseMove(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.GUIDE) {
					ui.getGuideUi().handleMouseMove(e.getX(), e.getY());
				} else if (SystemController.getCurrentState() == SystemController.GameState.START_GAME) {
					if (ui != null && ui.getStartGameUi() != null) {
						ui.getStartGameUi().handleMouseMove(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS) {
					if (ui != null && ui.getSettingsUi() != null) {
						ui.getSettingsUi().handleMouseMove(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.REMAPPING) { 
					if (ui != null && ui.getRemappingUi() != null) {
						ui.getRemappingUi().handleMouseMove(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.ACHIEVEMENTS) {
					if (ui != null && ui.getAchievementsUi() != null) {
						ui.getAchievementsUi().handleMouseMove(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.PAUSED) {
					if (ui != null && ui.getPauseUi() != null) {
						ui.getPauseUi().handleMouseMove(e.getX(), e.getY());
					}
				} else if (SystemController.getCurrentState() == SystemController.GameState.VICTORY) {
					if (ui != null && ui.getVictoryUi() != null) {
						ui.getVictoryUi().handleMouseMove(e.getX(), e.getY());
					}
				}
			}

			@Override
			public void mouseDragged(java.awt.event.MouseEvent e) {
				if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS) {
					if (ui != null && ui.getSettingsUi() != null) {
						ui.getSettingsUi().handleMouseDragged(e.getX(), e.getY());
					}
				}
			}
		});

	}
}