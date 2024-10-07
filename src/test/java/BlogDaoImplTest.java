package com.JAVA.DAO;

import com.JAVA.Beans.Blog;
import com.JAVA.Beans.Comment;
import com.JAVA.Beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogDaoImpl implements BlogDAO {

    private DAOFactory daoFactory;
    private UserDaoImpl userDaoImpl; // Changed to instance variable

    // Constructor
    public BlogDaoImpl(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.userDaoImpl = new UserDaoImpl(daoFactory); // Initialize here
    }

    private static Blog map(ResultSet resultSet) throws SQLException {
        Blog blog = new Blog();
        blog.setBlogId(resultSet.getInt("blog_id"));
        blog.setTitle(resultSet.getString("title"));
        blog.setDescription(resultSet.getString("description"));
        blog.setPicture(resultSet.getBytes("picture"));
        blog.setPublicationDate(resultSet.getTimestamp("publicationDate"));
        // Populate user information in the mapping
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setName(resultSet.getString("user_name"));
        user.setPassword(resultSet.getString("user_password"));
        user.setPicture(resultSet.getBytes("user_picture"));
        user.setEmail(resultSet.getString("user_email"));
        blog.setUser(user);
        return blog;
    }

    @Override
    public void addBlog(Blog blog) throws DAOException {
        validateUserAndForeignKey(blog);

        final String SQL_INSERT = "INSERT INTO blogs (title, description, picture, publicationDate, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
             
            preparedStatement.setString(1, blog.getTitle());
            preparedStatement.setString(2, blog.getDescription());
            preparedStatement.setBytes(3, blog.getPicture());

            // Set publicationDate, defaulting to now if null
            preparedStatement.setTimestamp(4, blog.getPublicationDate() != null 
                ? new Timestamp(blog.getPublicationDate().getTime()) 
                : new Timestamp(new Date().getTime()));

            preparedStatement.setLong(5, blog.getUser().getId());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating blog failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    blog.setBlogId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating blog failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    private void validateUserAndForeignKey(Blog blog) throws DAOException {
        if (!userExists(blog.getUser().getId())) {
            throw new DAOException("User does not exist. Cannot add the blog.");
        }
        // Foreign key constraint checks should be based on your application logic
        if (!foreignkeyConstraintIsSatisfied(blog.getUser().getId())) {
            throw new DAOException("Foreign key constraint violated. Cannot add the blog.");
        }
    }

    private boolean userExists(long userId) {
        final String SQL_USER_EXISTS = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_USER_EXISTS)) {
             
            preparedStatement.setLong(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean foreignkeyConstraintIsSatisfied(long userId) {
        // Implement actual foreign key constraint checks as needed
        return true; // Placeholder, replace with actual logic
    }

    @Override
    public Blog getBlogById(int blogId) throws DAOException {
        final String SQL_SELECT_BY_ID = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id " +
                "WHERE b.blog_id = ?";

        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = initRequestPrepare(connection, SQL_SELECT_BY_ID, blogId);
             ResultSet resultSet = preparedStatement.executeQuery()) {
             
            if (resultSet.next()) {
                return map(resultSet);
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }

        return null;
    }

    @Override
    public List<Blog> getBlogsByUserId(int userId) throws DAOException {
        final String SQL_SELECT_BY_USER_ID = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id " +
                "WHERE b.user_id = ?";

        List<Blog> blogList = new ArrayList<>();
        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = initRequestPrepare(connection, SQL_SELECT_BY_USER_ID, userId);
             ResultSet resultSet = preparedStatement.executeQuery()) {
             
            while (resultSet.next()) {
                blogList.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }

        return blogList;
    }

    @Override
    public List<Blog> searchBlogsByTitle(String title) throws DAOException {
        final String SQL_SEARCH_BY_TITLE = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id " +
                "WHERE b.title LIKE ?";

        List<Blog> blogList = new ArrayList<>();
        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = initRequestPrepare(connection, SQL_SEARCH_BY_TITLE, "%" + title + "%");
             ResultSet resultSet = preparedStatement.executeQuery()) {
             
            while (resultSet.next()) {
                blogList.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }

        return blogList;
    }

    @Override
    public void updateBlog(Blog blog) throws DAOException {
        final String SQL_UPDATE = "UPDATE blogs SET user_id = ?, title = ?, description = ?, picture = ? WHERE blog_id = ?";

        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE)) {
             
            preparedStatement.setInt(1, blog.getUser().getId());
            preparedStatement.setString(2, blog.getTitle());
            preparedStatement.setString(3, blog.getDescription());
            preparedStatement.setBytes(4, blog.getPicture());
            preparedStatement.setInt(5, blog.getBlogId());

            int rowsUpdated = preparedStatement.executeUpdate();
            System.out.println("Rows updated: " + rowsUpdated);
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public void deleteBlog(int blogId) throws DAOException {
        final String SQL_DELETE = "DELETE FROM blogs WHERE blog_id = ?";

        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = initRequestPrepare(connection, SQL_DELETE, blogId)) {
             
            preparedStatement.executeUpdate();
            System.out.println("Blog deleted successfully");
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    private static PreparedStatement initRequestPrepare(Connection connection, String sql, Object... objects) 
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
        return preparedStatement;
    }

    @Override
    public List<Blog> getAllBlogs() throws DAOException {
        final String SQL_SELECT_ALL = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id";

        List<Blog> blogList = new ArrayList<>();
        try (Connection connection = daoFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
             
            while (resultSet.next()) {
                blogList.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }

        return blogList;
    }
}
