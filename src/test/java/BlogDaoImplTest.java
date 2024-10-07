package com.JAVA.DAO;

import com.JAVA.Beans.Blog;
import com.JAVA.Beans.Comment;
import com.JAVA.Beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BlogDaoImplTest {

    private DAOFactory daoFactory;
    private BlogDaoImpl blogDao;
    private Connection connection;

    @BeforeEach
    public void setUp() throws SQLException {
        daoFactory = Mockito.mock(DAOFactory.class);
        connection = Mockito.mock(Connection.class);
        when(daoFactory.getConnection()).thenReturn(connection);
        blogDao = new BlogDaoImpl(daoFactory);
    }

    @Test
    public void testAddBlog() throws SQLException, DAOException {
        Blog blog = new Blog();
        blog.setTitle("Test Title");
        blog.setDescription("Test Description");
        User user = new User();
        user.setId(1); // Assuming user ID is 1
        blog.setUser(user);

        // Mock the PreparedStatement
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        when(connection.prepareStatement(any(String.class), anyInt())).thenReturn(preparedStatement);
        
        // Mock executeUpdate to return 1 (indicating success)
        when(preparedStatement.executeUpdate()).thenReturn(1);
        
        // Mock getGeneratedKeys to return a mock ResultSet
        ResultSet generatedKeys = Mockito.mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(1L); // Mock the generated key

        // Call the method under test
        blogDao.addBlog(blog);

        // Verify that the appropriate methods were called on the preparedStatement
        verify(preparedStatement, times(1)).setString(1, "Test Title");
        verify(preparedStatement, times(1)).setString(2, "Test Description");
        verify(preparedStatement, times(1)).setLong(3, user.getId()); // Ensure the correct index for user ID
    }

    @Test
    public void testGetBlogById() throws SQLException, DAOException {
        Blog expectedBlog = new Blog();
        expectedBlog.setBlogId(1);
        expectedBlog.setTitle("Test Title");
        expectedBlog.setDescription("Test Description");

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("blog_id")).thenReturn(expectedBlog.getBlogId());
        when(resultSet.getString("title")).thenReturn(expectedBlog.getTitle());
        when(resultSet.getString("description")).thenReturn(expectedBlog.getDescription());

        Blog actualBlog = blogDao.getBlogById(1);

        assertEquals(expectedBlog.getBlogId(), actualBlog.getBlogId());
        assertEquals(expectedBlog.getTitle(), actualBlog.getTitle());
        assertEquals(expectedBlog.getDescription(), actualBlog.getDescription());
    }

    @Test
    public void testGetBlogsByUserId() throws SQLException, DAOException {
        List<Blog> expectedBlogs = new ArrayList<>();
        Blog blog1 = new Blog();
        blog1.setBlogId(1);
        blog1.setTitle("Test Title 1");
        expectedBlogs.add(blog1);
        
        Blog blog2 = new Blog();
        blog2.setBlogId(2);
        blog2.setTitle("Test Title 2");
        expectedBlogs.add(blog2);

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("blog_id")).thenReturn(1, 2);
        when(resultSet.getString("title")).thenReturn("Test Title 1", "Test Title 2");

        List<Blog> actualBlogs = blogDao.getBlogsByUserId(1);

        assertEquals(expectedBlogs.size(), actualBlogs.size());
        assertEquals(expectedBlogs.get(0).getTitle(), actualBlogs.get(0).getTitle());
        assertEquals(expectedBlogs.get(1).getTitle(), actualBlogs.get(1).getTitle());
    }

   

    @Test
    public void testGetAllBlogs() throws SQLException, DAOException {
        List<Blog> expectedBlogs = new ArrayList<>();
        Blog blog1 = new Blog();
        blog1.setBlogId(1);
        blog1.setTitle("Test Title 1");
        expectedBlogs.add(blog1);

        Blog blog2 = new Blog();
        blog2.setBlogId(2);
        blog2.setTitle("Test Title 2");
        expectedBlogs.add(blog2);

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("blog_id")).thenReturn(1, 2);
        when(resultSet.getString("title")).thenReturn("Test Title 1", "Test Title 2");

        List<Blog> actualBlogs = blogDao.getAllBlogs();

        assertEquals(expectedBlogs.size(), actualBlogs.size());
        assertEquals(expectedBlogs.get(0).getTitle(), actualBlogs.get(0).getTitle());
        assertEquals(expectedBlogs.get(1).getTitle(), actualBlogs.get(1).getTitle());
    }

    @Test
    public void testGetCommentsByBlogId() throws SQLException, DAOException {
        List<Comment> expectedComments = new ArrayList<>();
        Comment comment = new Comment();
        comment.setCommentId(1);
        comment.setContent("Test Comment");
        expectedComments.add(comment);

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("comment_id")).thenReturn(1);
        when(resultSet.getString("content")).thenReturn("Test Comment");

        List<Comment> actualComments = blogDao.getCommentsByBlogId(1);

        assertEquals(expectedComments.size(), actualComments.size());
        assertEquals(expectedComments.get(0).getContent(), actualComments.get(0).getContent());
    }
}
