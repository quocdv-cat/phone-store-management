package com.phonestore.service;

import com.phonestore.dao.CategoryDAO;
import com.phonestore.model.Category;
import com.phonestore.utils.ValidationUtils;
import java.util.List;

/**
 * Xử lý nghiệp vụ CRUD cho Danh mục điện thoại (Category).
 */
public class CategoryService {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryDAO.findAllActive();
    }

    public List<Category> searchCategories(String keyword) {
        return categoryDAO.search(keyword);
    }

    public String addCategory(Category category) {
        if (ValidationUtils.isEmpty(category.getCategoryName())) {
            return "Tên danh mục không được để trống!";
        }
        if (categoryDAO.existsByName(category.getCategoryName(), 0)) {
            return "Tên danh mục '" + category.getCategoryName().trim() + "' đã tồn tại!";
        }
        return categoryDAO.insert(category) ? null : "Không thể thêm danh mục vào cơ sở dữ liệu!";
    }

    public String updateCategory(Category category) {
        if (category.getCategoryId() <= 0) {
            return "Vui lòng chọn một danh mục từ bảng để cập nhật!";
        }
        if (ValidationUtils.isEmpty(category.getCategoryName())) {
            return "Tên danh mục không được để trống!";
        }
        if (categoryDAO.existsByName(category.getCategoryName(), category.getCategoryId())) {
            return "Tên danh mục '" + category.getCategoryName().trim() + "' đã tồn tại!";
        }
        return categoryDAO.update(category) ? null : "Không thể cập nhật danh mục!";
    }

    public String deleteCategory(int categoryId) {
        if (categoryId <= 0) {
            return "Vui lòng chọn danh mục cần xóa!";
        }
        int productCount = categoryDAO.countProductsByCategory(categoryId);
        if (productCount > 0) {
            return "Không thể xóa danh mục này vì đang có " + productCount + " sản phẩm liên kết!";
        }
        return categoryDAO.delete(categoryId) ? null : "Không thể xóa danh mục!";
    }
}
