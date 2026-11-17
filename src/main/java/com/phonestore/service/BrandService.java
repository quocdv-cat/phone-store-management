package com.phonestore.service;

import com.phonestore.dao.BrandDAO;
import com.phonestore.model.Brand;
import com.phonestore.utils.ValidationUtils;
import java.util.List;

/**
 * Xử lý nghiệp vụ CRUD cho Thương hiệu điện thoại (Brand).
 */
public class BrandService {

    private final BrandDAO brandDAO = new BrandDAO();

    public List<Brand> getAllBrands() {
        return brandDAO.findAll();
    }

    public List<Brand> getActiveBrands() {
        return brandDAO.findAllActive();
    }

    public List<Brand> searchBrands(String keyword) {
        return brandDAO.search(keyword);
    }

    public String addBrand(Brand brand) {
        if (ValidationUtils.isEmpty(brand.getBrandName())) {
            return "Tên thương hiệu không được để trống!";
        }
        if (brandDAO.existsByName(brand.getBrandName(), 0)) {
            return "Tên thương hiệu '" + brand.getBrandName().trim() + "' đã tồn tại!";
        }
        return brandDAO.insert(brand) ? null : "Không thể thêm thương hiệu vào cơ sở dữ liệu!";
    }

    public String updateBrand(Brand brand) {
        if (brand.getBrandId() <= 0) {
            return "Vui lòng chọn một thương hiệu từ bảng để cập nhật!";
        }
        if (ValidationUtils.isEmpty(brand.getBrandName())) {
            return "Tên thương hiệu không được để trống!";
        }
        if (brandDAO.existsByName(brand.getBrandName(), brand.getBrandId())) {
            return "Tên thương hiệu '" + brand.getBrandName().trim() + "' đã tồn tại!";
        }
        return brandDAO.update(brand) ? null : "Không thể cập nhật thương hiệu!";
    }

    public String deleteBrand(int brandId) {
        if (brandId <= 0) {
            return "Vui lòng chọn thương hiệu cần xóa!";
        }
        int productCount = brandDAO.countProductsByBrand(brandId);
        if (productCount > 0) {
            return "Không thể xóa thương hiệu này vì đang có " + productCount + " sản phẩm liên kết!";
        }
        return brandDAO.delete(brandId) ? null : "Không thể xóa thương hiệu!";
    }
}
