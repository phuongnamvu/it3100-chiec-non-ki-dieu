package model;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import connectionDB.ConnectDB;
import controller.ControllerClickButton;
import controller.ControllerGame;
import view.GameJframe;
import view.NoticeMessage;
import view.SpecialGameRound;

public class PlayGame extends ConnectDB {
	private PreparedStatement stmt;
	private ResultSet rs;

	/**
	 * Kiểm tra đáp án của người chơi với đáp án câu hỏi
	 * 
	 * @param question : đối tượng câu hỏi
	 * @param round : vòng đấu
	 * @return notice : thông báo
	 * @throws SQLException
	 */
	public String checkDapan(Question question, String answer, int round) throws SQLException {
		conn = openConnectDB();
		String notice = "";
		int parameterIndex = 1;
		String sql = "select questionid from question "
				   + "where dapantv = ? and questionid = ? ";
		if (round == 4) {
			sql = "select questionspecialid from questionspecial "
				+ "where dapantv = ? and questionspecialid = ? ";
		}
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(parameterIndex++, answer);
			stmt.setInt(parameterIndex++, question.getQuestionid());
			rs = stmt.executeQuery();
			if (rs.next()) {
				notice = "Chính xác";
			} else {
				notice = "Rất tiếc bạn đã trả lời sai";
			}
		} catch (Exception e) {
			NoticeMessage.noticeMessage("Hệ thống đang có lỗi");
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return notice;
	}

	/**
	 * Kiểm tra ô chữ người chơi đoán
	 * @param question : đối tượng câu hỏi
	 * @param round : vòng đấu
	 * @return notice : thông báo
	 * @throws SQLException
	 */
	public int checkOChu(Question question, String dapanPlayer, int round) throws SQLException {
		conn = openConnectDB();
		int count = 0;
		int parameterIndex = 1;
		String sql = "select questionid, dapan from question "
				   + "where questionid= ? and dapan like ? ";
		if (round == 4) {
			sql = "select questionspecialid, dapan from questionspecial "
				+ "where questionspecialid= ? and dapan like ? ";
		}
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(parameterIndex++, question.getQuestionid());
			stmt.setString(parameterIndex++, "%" + dapanPlayer + "%");
			rs = stmt.executeQuery();
			if (!rs.next()) {
				count = 0;
			} else {
				String dapan = rs.getString("dapan");
				for (int i = 0; i < dapan.length(); i++) {
					if (dapanPlayer.equals(String.valueOf(dapan.charAt(i)))) {
						count++;
					}
				}
			}
		} catch (Exception e) {
			NoticeMessage.noticeMessage("Hệ thống đang có lỗi");
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return count;
	}

	/**
	 * tìm vị trí ô chữ trong đáp án
	 * 
	 * @param question : thông tin câu hỏi
	 * @return vị trí ô chữ
	 */
	public ArrayList<Integer> locationOChu(Question question, String dapanPlayer) {
		ArrayList<Integer> location = new ArrayList<>();
		String dapan = question.getDapan();
		for (int i = 0; i < dapan.length(); i++) {
			if (dapanPlayer.equals(String.valueOf(dapan.charAt(i)))) {
				location.add(i);
			}
		}
		return location;
	}

	/**
	 * lấy thông tin câu hỏi theo topic
	 * 
	 * @param topic : chủ đề
	 * @return arraylistQuestion : list câu hỏi
	 * @throws SQLException
	 */
	public ArrayList<Question> getQuestionInforByTopic(String topic) throws SQLException {
		ArrayList<Question> arrayQuestion = new ArrayList<>();
		Question question;
		conn = openConnectDB();
		int parameterIndex = 1;
		String sql = "select questionid, question, dapan, dapantv " + "from question " + "where topic = ? ";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(parameterIndex++, topic);
			rs = stmt.executeQuery();
			while (rs.next()) {
				question = new Question();
				question.setQuestionid(Integer.valueOf(rs.getString("questionid")));
				question.setQuestion(rs.getString("question"));
				question.setDapan(rs.getString("dapan"));
				question.setDapantv(rs.getString("dapantv"));
				arrayQuestion.add(question);
			}
		} catch (Exception e) {
			NoticeMessage.noticeMessage("Hệ thống đang có lỗi");
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return arrayQuestion;
	}

	/**
	 * lấy thông tin câu hỏi vòng đặc biệt
	 * 
	 * @return arraylistQuestion : list câu hỏi
	 * @throws SQLException
	 */
	public ArrayList<Question> getQuestionSpecial() throws SQLException {
		ArrayList<Question> arrayQuestion = new ArrayList<>();
		Question question;
		conn = openConnectDB();
		String sql = "select questionspecialid, question, dapan, dapantv "
				   + "from questionspecial";
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				question = new Question();
				question.setQuestionid(Integer.valueOf(rs.getString("questionspecialid")));
				question.setQuestion(rs.getString("question"));
				question.setDapan(rs.getString("dapan"));
				question.setDapantv(rs.getString("dapantv"));
				arrayQuestion.add(question);
			}
		} catch (Exception e) {
			NoticeMessage.noticeMessage("Hệ thống đang có lỗi");
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return arrayQuestion;
	}

	/**
	 * Random câu hỏi từ arraylist câu hỏi
	 * 
	 * @param arrayQuestion
	 *            : arraylist câu hỏi
	 * @return question : thông tin câu hỏi
	 */
	public Question randomQuestion(ArrayList<Question> arrayQuestion) {
		Question question = new Question();
		Random rn = new Random();
		question = arrayQuestion.get(rn.nextInt(arrayQuestion.size()));
		return question;
	}

	/**
	 * tạo số ô đáp án cho câu hỏi
	 * 
	 * @return count : số ô cần tạo
	 */
	public int countOChu(Question question) {
		String dapan = question.getDapan();
		int count = dapan.length();
		return count;
	}

	/**
	 * chuyển đổi sang chữ cái
	 * 
	 * @param text
	 *            : text đổi
	 * @return chữ cái
	 */
	public String convertText(String text) {
		if ("00".equals(text)) {
			return "A";
		} else if ("01".equals(text)) {
			return "B";
		} else if ("02".equals(text)) {
			return "C";
		} else if ("03".equals(text)) {
			return "D";
		} else if ("04".equals(text)) {
			return "E";
		} else if ("05".equals(text)) {
			return "F";
		} else if ("06".equals(text)) {
			return "G";
		} else if ("07".equals(text)) {
			return "H";
		} else if ("08".equals(text)) {
			return "I";
		} else if ("09".equals(text)) {
			return "J";
		} else if ("010".equals(text)) {
			return "K";
		} else if ("011".equals(text)) {
			return "L";
		} else if ("012".equals(text)) {
			return "M";
		} else if ("10".equals(text)) {
			return "N";
		} else if ("11".equals(text)) {
			return "O";
		} else if ("12".equals(text)) {
			return "P";
		} else if ("13".equals(text)) {
			return "Q";
		} else if ("14".equals(text)) {
			return "R";
		} else if ("15".equals(text)) {
			return "S";
		} else if ("16".equals(text)) {
			return "T";
		} else if ("17".equals(text)) {
			return "U";
		} else if ("18".equals(text)) {
			return "V";
		} else if ("19".equals(text)) {
			return "W";
		} else if ("110".equals(text)) {
			return "X";
		} else if ("111".equals(text)) {
			return "Y";
		} else if ("112".equals(text)) {
			return "Z";
		}
		return text;
	}

	/**
	 * khóa ô chữ
	 * 
	 * @param notice : thông báo
	 * @return true khóa thành công và ngược lại
	 */
	public boolean lock(String notice) {
		if ("Chính xác".equals(notice) || "Time Out".equals(notice) || "Đoán hết 3 ô chữ".equals(notice)) {
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 13; j++) {
					ControllerGame.gameJframe.buttonPlay[i][j].setEnabled(false);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * set color lượt chơi của người chơi
	 */
	public static void setLuotChoi() {
		if (ControllerClickButton.luotchoi == 0) {
			GameJframe.label[4].setForeground(Color.RED);
			GameJframe.label[6].setForeground(Color.black);
		} else if (ControllerClickButton.luotchoi == 1) {
			GameJframe.label[5].setForeground(Color.RED);
			GameJframe.label[4].setForeground(Color.black);
		} else if (ControllerClickButton.luotchoi == 2) {
			GameJframe.label[6].setForeground(Color.RED);
			GameJframe.label[5].setForeground(Color.black);
		}
	}
	/**
	 * đổi lượt chơi
	 */
	public static void swapLuotChoi() {
		if (ControllerClickButton.luotchoi == 2) {
			ControllerClickButton.luotchoi = 0;
		} else {
			ControllerClickButton.luotchoi++;
		}
	}
}
