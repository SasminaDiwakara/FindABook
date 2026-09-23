import lk.jiat.fiadabook.util.HibernateUtil;
import org.hibernate.Session;

public class test {
    public static void main(String[] args) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.close();
    }
}
