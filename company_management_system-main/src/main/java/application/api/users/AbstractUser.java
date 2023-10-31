package application.api.users;

import application.StandardUserPhoto;
import application.api.Database;
import application.api.exceptions.users.IncorrectPasswordException;
import application.api.exceptions.users.InvalidPasswordException;
import application.api.exceptions.users.InvalidUpdateFieldsException;
import application.api.exceptions.users.PasswordsDontMatchException;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

interface AbstractUserInterface {
    void changePassword(String oldPassword, String newPassword, String passwordConfirm)
            throws SQLException, PasswordsDontMatchException, InvalidPasswordException, IncorrectPasswordException;
    void changePhoto(String path) throws SQLException, InvalidUpdateFieldsException;
    void deletePhoto() throws SQLException;
}

abstract public class AbstractUser implements AbstractUserInterface {
    private final Database db = Database.getInstance();
    @Override
    public void changePassword(String oldPassword, String newPassword, String passwordConfirm)
    throws SQLException, PasswordsDontMatchException, InvalidPasswordException, IncorrectPasswordException
    {
        // function where user can update their password

        Authorization auth = new Authorization();
        String userId = CurrentUser.getUserId();

        String hashedOldPassword = auth.hash(oldPassword);
        ResultSet resultSet = db.get("password", "users", "user_id = " + userId);

        // check if old password is correct
        if (resultSet.next()){
            if (!hashedOldPassword.equals(resultSet.getString("password"))){
                throw new IncorrectPasswordException("Old password must be correct");
            }
        }

        if (!Objects.equals(newPassword, passwordConfirm)) throw new PasswordsDontMatchException("Passwords must match");

        if (newPassword.length() < 8) throw new InvalidPasswordException("Password must be at least 8 characters");

        String generatedPassword = auth.hash(newPassword);

        db.update("users", "user_id = " + userId, "password = '" + generatedPassword + "'");

    }

    @Override
    public void deletePhoto() throws SQLException {
        db.update("users", "user_id = " + CurrentUser.getUserId(), "photo = '" +
                StandardUserPhoto.path +"'");
    }

    @Override
    public void changePhoto(String path) throws SQLException, InvalidUpdateFieldsException {
        try {
            File f = new File(path);
            f.getCanonicalPath();
            db.update("users", "user_id = " + CurrentUser.getUserId(), "photo = '" + path + "'");
        }
        catch (Exception exception){
            throw new InvalidUpdateFieldsException("Invalid file");
        }
    }

}
