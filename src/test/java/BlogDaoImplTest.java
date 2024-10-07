package com.JAVA.DAO;

import com.JAVA.Beans.Blog;
import com.JAVA.Beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BlogDaoImplTest {

    @Mock
    private DAOFactory daoFactory;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private BlogDaoImpl blogDao;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(daoFactory.getConnection()).thenReturn(connection);
    }

    @Test
    public void testAddBlog() throws SQLException, DAOException {
        Blog blog = new Blog();
        blog.setTitle("Test Title");
        blog.setDescription("Test Description");
        blog.setUser(new User());
        blog.getUser().setId(1L);

        String sql = "INSERT INTO blogs (title, description, picture, publicationDate, user_id) VALUES (?, ?, ?, ?, ?)";
        when(connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // Simulate generated ID

        blogDao.addBlog(blog);

        assertEquals(1, blog.getBlogId());
        verify(preparedStatement).setString(1, blog.getTitle());
        verify(preparedStatement).setString(2, blog.getDescription());
        verify(preparedStatement).setLong(5, blog.getUser().getId());
    }

    @Test
    public void testGetBlogById() throws SQLException, DAOException {
        int blogId = 1;
        Blog expectedBlog = new Blog();
        expectedBlog.setBlogId(blogId);
        expectedBlog.setTitle("Test Title");
        expectedBlog.setDescription("Test Description");
        expectedBlog.setUser(new User());

        String sql = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, u.id as user_id FROM blogs b INNER JOIN users u ON b.user_id = u.id WHERE b.blog_id = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("blog_id")).thenReturn(expectedBlog.getBlogId());
        when(resultSet.getString("title")).thenReturn(expectedBlog.getTitle());
        when(resultSet.getString("description")).thenReturn(expectedBlog.getDescription());

        Blog actualBlog = blogDao.getBlogById(blogId);

        assertEquals(expectedBlog.getBlogId(), actualBlog.getBlogId());
        assertEquals(expectedBlog.getTitle(), actualBlog.getTitle());
        assertEquals(expectedBlog.getDescription(), actualBlog.getDescription());
    }

    @Test
    public void testGetAllBlogs() throws SQLException, DAOException {
        String sql = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, u.id as user_id FROM blogs b INNER JOIN users u ON b.user_id = u.id";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false); // Simulate one blog returned

        when(resultSet.getInt("blog_id")).thenReturn(1);
        when(resultSet.getString("title")).thenReturn("Test Title");
        when(resultSet.getString("description")).thenReturn("Test Description");

        List<Blog> blogs = blogDao.getAllBlogs();

        assertEquals(1, blogs.size());
        assertEquals("Test Title", blogs.get(0).getTitle());
    }

    @Test
    public void testDeleteBlog() throws SQLException, DAOException {
        int blogId = 1;

        String sql = "DELETE FROM blogs WHERE blog_id = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        blogDao.deleteBlog(blogId);

        verify(preparedStatement).setInt(1, blogId);
        verify(preparedStatement).executeUpdate();
    }

    // Add more tests for the other methods in BlogDaoImpl
}
