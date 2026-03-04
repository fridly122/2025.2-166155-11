import java.util.Scanner;   // Import thư viện để nhập từ bàn phím

public class HelloWorld {      // Khai báo class

    public static void main(String[] args) {   // Hàm main

        Scanner sc = new Scanner(System.in);  // Tạo đối tượng Scanner để đọc input

        System.out.print("Enter your name: "); // In yêu cầu nhập

        String name = sc.nextLine();           // Đọc cả dòng người dùng nhập

        System.out.println("Hello " + name);   // In lời chào

        sc.close();                            // Đóng Scanner
    }
}