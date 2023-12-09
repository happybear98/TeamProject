package DB_STATEMENT;

import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.*;

public class Statement_Func {
    Connection con = null;
    String url = "jdbc:oracle:thin:@localhost:1521:XE";
    String id = "soccer";
    String password = "1234";
    static Scanner scanner = new Scanner(System.in);
    public Statement_Func() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("드라이버 적재 성공");
        } catch (ClassNotFoundException e) { System.out.println("No Driver"); }
    }
    private void DB_connect() {
        try {
            con = DriverManager.getConnection(url, id, password);
            System.out.println("DB연결 성공");
        } catch (SQLException e) { System.out.println("Connection Fail"); }
    }
    private void find_Team_Player() throws SQLException{
        System.out.println("1. 구단 검색\n2. 선수 검색");
        System.out.print("검색할 항목을 선택하세요(1 또는 2): ");
        int option = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비

        if (option == 1) {
            try {
                System.out.print("구단 이름을 입력하세요: ");
                String teamName = scanner.nextLine();
                DB_connect();
                Statement stmt = con.createStatement();
                String teamQuery = "SELECT * FROM 구단 WHERE 구단이름 = '" + teamName + "'" ;
                String tpQuery = "SELECT * FROM 선수 WHERE 구단이름 = '" + teamName + "'";
                ResultSet teamResult = stmt.executeQuery(teamQuery);
                ResultSet teamPlayerResult = stmt.executeQuery(tpQuery);

                while (teamResult.next()) {
                    System.out.print("\t" + teamResult.getString("구단이름"));
                    System.out.print("\t" + teamResult.getString("소유경기장"));
                    System.out.print("\t" + teamResult.getInt("전체선수수"));
                    System.out.print("\t" + teamResult.getInt("총승리수"));
                    System.out.print("\t" + teamResult.getInt("총패배수" + "\n"));
                }
                while (teamPlayerResult.next()) {
                    System.out.print("\t" + teamPlayerResult.getString("선수번호"));
                    System.out.print("\t" + teamPlayerResult.getString("선수이름"));
                    System.out.print("\t" + teamPlayerResult.getString("포지션"));
                    System.out.print("\t" + teamPlayerResult.getInt("출전횟수"));
                    System.out.print("\t" + teamPlayerResult.getInt("총득점수" + "\n\n"));
                }
                stmt.close(); teamResult.close(); teamPlayerResult.close();
            } catch (SQLException e) { e.printStackTrace();
            }finally { con.close(); }

        } else if (option == 2) {
            System.out.print("선수 이름을 입력하세요: ");
            String playerName = scanner.nextLine();

            try {
                DB_connect();
                Statement stmt = con.createStatement();
                String playerQuery = "SELECT * FROM 선수 WHERE 선수이름 = '" + playerName + "'";
                ResultSet playerResult = stmt.executeQuery(playerQuery);

                while (playerResult.next()) {
                    System.out.print("\t" + playerResult.getString("선수번호"));
                    System.out.print("\t" + playerResult.getString("선수이름"));
                    System.out.print("\t" + playerResult.getString("포지션"));
                    System.out.print("\t" + playerResult.getInt("출전횟수"));
                    System.out.print("\t" + playerResult.getInt("총득점수") + "\n\n");
                }
                stmt.close(); playerResult.close();
            } catch (SQLException e) { e.printStackTrace();
            }finally { con.close(); }
        } else { System.out.println("1 또는 2만 입력해주세요."); }
    }

    public void new_Player() {
        try {
            DB_connect();

            System.out.println("\n추가할 선수 정보를 입력하세요.");
            System.out.print("선수 번호: ");
            int playerNumber = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비
            System.out.print("구단 이름: ");
            String teamrName = scanner.nextLine();
            System.out.print("선수 이름: ");
            String playerName = scanner.nextLine();
            System.out.print("포지션: ");
            String position = scanner.nextLine();
            System.out.print("출전 횟수: ");
            int appearances = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비
            System.out.print("총 득점수: ");
            int goals = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비

            String insertQuery = "INSERT INTO 선수 (선수번호, 구단이름, 선수이름, 포지션, 출전횟수, 총득점수) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(insertQuery);

            pstmt.setInt(1, playerNumber);
            pstmt.setString(2, teamrName);
            pstmt.setString(3, playerName);
            pstmt.setString(4, position);
            pstmt.setInt(5, appearances);
            pstmt.setInt(6, goals);

            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0) {
                System.out.println("새로운 선수가 정상적으로 추가되었습니다.");
            } else {
                System.out.println("선수 추가에 실패하였습니다.");
            }
            pstmt.close(); con.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void update_Game_Schedule_n_Significant() {
        try {
            DB_connect();
            System.out.println("\n경기 일정을 선택합니다.");
            System.out.print("경기 날짜(YYYY-MM-DD): ");
            String gameDateStr = scanner.nextLine();
            java.sql.Date gameDate = java.sql.Date.valueOf(gameDateStr);
            System.out.print("경기장: ");
            String stadium = scanner.nextLine();
            System.out.print("홈 팀 이름: ");
            String homeTeam = scanner.nextLine();
            System.out.print("어웨이 팀 이름: ");
            String awayTeam = scanner.nextLine();

            CallableStatement cstmt = con.prepareCall("{call S_UpdateGame(?, ?, ?, ?)}");
            cstmt.setDate(1, gameDate);
            cstmt.setString(2, stadium);
            cstmt.setString(3, homeTeam);
            cstmt.setString(4, awayTeam);

            cstmt.executeUpdate();
            System.out.println("경기 일정이 업데이트되었습니다.");
            cstmt.close();

            cstmt = con.prepareCall("{call S_GetTeamPlayers(?, ?, ?, ?)}");
            cstmt.setString(1, homeTeam);
            cstmt.setString(2, awayTeam);

            // OUT 파라미터를 위한 ResultSet
            cstmt.registerOutParameter(3, OracleTypes.CURSOR);
            cstmt.registerOutParameter(4, OracleTypes.CURSOR);

            // 프로시저 실행
            cstmt.execute();

            // OUT 파라미터에서 결과를 가져와 출력
            ResultSet homeTeamPlayers = (ResultSet) cstmt.getObject(3);
            ResultSet awayTeamPlayers = (ResultSet) cstmt.getObject(4);

            List<String> homePlayers = new ArrayList<>();
            List<String> awayPlayers = new ArrayList<>();

            System.out.println("홈 팀 선수:");
            while (homeTeamPlayers.next()) {
                System.out.println("선수 번호: " + homeTeamPlayers.getInt("선수번호") + ", 선수 이름: " + homeTeamPlayers.getString("선수이름"));
                homePlayers.add(homeTeamPlayers.getString("선수이름"));
            }

            System.out.println("어웨이 팀 선수:");
            while (awayTeamPlayers.next()) {
                System.out.println("선수 번호: " + awayTeamPlayers.getInt("선수번호") + ", 선수 이름: " + awayTeamPlayers.getString("선수이름"));
                awayPlayers.add(awayTeamPlayers.getString("선수이름"));
            }

            String team = null; String player = null;
            while (true) {
                System.out.print("홈 또는 어웨이 선택(또는 'exit' 입력): ");
                String select = scanner.nextLine();
                if (select.equals("홈")) {
                    team = homeTeam;
                    System.out.print("선수이름 선택: ");
                    player = scanner.nextLine();
                } else if (select.equals("어웨이")) {
                    team = awayTeam;
                    System.out.print("선수이름 선택: ");
                    player = scanner.nextLine();
                } else if (select.equalsIgnoreCase("exit")) {
                    break;
                } else { System.out.println("다시 입력해주세요"); }

                System.out.print("득점 수: ");
                int goals = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비
                System.out.print("경고 여부(없을 시 생략): ");
                int warning = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비
                System.out.print("퇴장 여부(없을 시 생략): ");
                int ejection = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비
                System.out.print("부상 여부(없을 시 생략): ");
                int injury = scanner.nextInt(); scanner.nextLine();  // 개행 문자 소비

                // 출전선수 테이블에 정보 입력
                insert_Player_Significant(team, player, gameDate, stadium, goals, warning, ejection, injury);
                homeTeamPlayers.close();
                awayTeamPlayers.close();
                cstmt.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void insert_Player_Significant(String tname,String pname, java.sql.Date mdate, String stadium, int goals, int warning, int injury, int ejection) throws SQLException {
        String query = "INSERT INTO 출전선수 (구단이름, 선수이름, 경기일, 경기장소, 경기득점수, 선수경고, 퇴장, 부상) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = con.prepareStatement(query);

        pstmt.setString(1, tname);
        pstmt.setString(2, pname);
        pstmt.setDate(3, mdate);
        pstmt.setString(4, stadium);
        pstmt.setInt(5, goals);
        pstmt.setInt(6, warning);
        pstmt.setInt(7, injury);
        pstmt.setInt(8, ejection);

        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("선수 성적이 추가되었습니다.");
        } else {
            System.out.println("선수 성적 추가 실패.");
        }

        pstmt.close(); con.close();
    }


    public static void main(String[] arg) throws SQLException {
        Statement_Func stfc = new Statement_Func();

        while(true) {
            System.out.print("\n\n기능 선택(1 or 2 or 3): ");
            int funcNum = scanner.nextInt();
            scanner.nextLine();  // 개행 문자 소비

            switch (funcNum) {
                case 1:
                    stfc.find_Team_Player();
                    break;
                case 2:
                    stfc.new_Player();
                    break;
                case 3:
                    stfc.update_Game_Schedule_n_Significant();
                    break;
                default:
                    System.out.println("기능은 1, 2, 3 중 하나만 선택 가능합니다.");
                    break;
            }
        }

    }
}



