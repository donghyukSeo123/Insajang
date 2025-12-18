/**
=========================================================
* Material Dashboard 2 React - v2.2.0
=========================================================

* Product Page: https://www.creative-tim.com/product/material-dashboard-react
* Copyright 2023 Creative Tim (https://www.creative-tim.com)

Coded by www.creative-tim.com

 =========================================================

* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
*/

import { useEffect, useState } from "react";

// react-router components
import { useLocation } from "react-router-dom";

// prop-types is a library for typechecking of props.
import PropTypes from "prop-types";

// @material-ui core components
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Menu from "@mui/material/Menu";
import Icon from "@mui/material/Icon";
import MDTypography from "components/MDTypography";
// Material Dashboard 2 React components
import MDBox from "components/MDBox";

// Material Dashboard 2 React example components
import NotificationItem from "examples/Items/NotificationItem";

// Custom styles for DashboardNavbar
import { navbar, navbarContainer, navbarRow } from "examples/Navbars/DashboardNavbar/styles";

// Material Dashboard 2 React context
import {
  setMiniSidenav,
  setOpenConfigurator,
  setTransparentNavbar,
  useMaterialUIController,
} from "context";

function DashboardNavbar({ absolute, light, isMini }) {
  const [navbarType, setNavbarType] = useState();
  const [controller, dispatch] = useMaterialUIController();
  const { miniSidenav, transparentNavbar, fixedNavbar, openConfigurator, darkMode } = controller;
  const [openMenu, setOpenMenu] = useState(false);
  const route = useLocation().pathname.split("/").slice(1);

  useEffect(() => {
    // Setting the navbar type
    if (fixedNavbar) {
      setNavbarType("sticky");
    } else {
      setNavbarType("static");
    }

    // A function that sets the transparent state of the navbar.
    function handleTransparentNavbar() {
      setTransparentNavbar(dispatch, (fixedNavbar && window.scrollY === 0) || !fixedNavbar);
    }

    /**
     The event listener that's calling the handleTransparentNavbar function when
     scrolling the window.
    */
    window.addEventListener("scroll", handleTransparentNavbar);

    // Call the handleTransparentNavbar function to set the state with the initial value.
    handleTransparentNavbar();

    // Remove event listener on cleanup
    return () => window.removeEventListener("scroll", handleTransparentNavbar);
  }, [dispatch, fixedNavbar]);

  const handleMiniSidenav = () => setMiniSidenav(dispatch, !miniSidenav);
  const handleConfiguratorOpen = () => setOpenConfigurator(dispatch, !openConfigurator);
  const handleOpenMenu = (event) => setOpenMenu(event.currentTarget);
  const handleCloseMenu = () => setOpenMenu(false);

  // Render the notifications menu
  const renderMenu = () => (
    <Menu
      anchorEl={openMenu}
      anchorReference={null}
      anchorOrigin={{
        vertical: "bottom",
        horizontal: "left",
      }}
      open={Boolean(openMenu)}
      onClose={handleCloseMenu}
      sx={{ mt: 2 }}
    >
      <NotificationItem icon={<Icon>email</Icon>} title="Check new messages" />
      <NotificationItem icon={<Icon>podcasts</Icon>} title="Manage Podcast sessions" />
      <NotificationItem icon={<Icon>shopping_cart</Icon>} title="Payment successfully completed" />
    </Menu>
  );

  // Styles for the navbar icons
  const iconsStyle = ({ palette: { dark, white, text }, functions: { rgba } }) => ({
    color: () => {
      let colorValue = light || darkMode ? white.main : dark.main;

      if (transparentNavbar && !light) {
        colorValue = darkMode ? rgba(text.main, 0.6) : text.main;
      }

      return colorValue;
    },
  });

  return (
    <AppBar
      position={absolute ? "absolute" : navbarType}
      color="inherit"
      sx={(theme) => navbar(theme, { transparentNavbar, absolute, light, darkMode })}
    >
      <Toolbar sx={(theme) => navbarContainer(theme)}>
        <MDBox color="inherit" mb={{ xs: 1, md: 0 }} sx={(theme) => navbarRow(theme, { isMini })}>
          {/*<Breadcrumbs icon="home" title={route[route.length - 1]} route={route} light={light} />*/}
        </MDBox>
        {/* 상단 중앙 전광판 섹션 */}
        <MDBox
          display={{ xs: "none", lg: "flex" }} // 모바일은 숨기고 PC에서만 노출
          flex={1}
          justifyContent="center"
        >
          <MDBox
            display="flex"
            alignItems="center"
            px={2}
            py={0.8}
            sx={{
              backgroundColor: ({ palette: { background } }) => background.default,
              borderRadius: "50px", // 알약 모양
              boxShadow: ({ boxShadows: { sm } }) => sm, // 은은한 그림자
              border: "1px solid #f0f2f5",
            }}
          >
            {/* 1. 분석 엔진 배지 (파이썬 서버 상태) */}
            <MDBox display="flex" alignItems="center" px={1.5}>
              <MDBox
                width="8px"
                height="8px"
                bgcolor="success.main" // 가동중일 땐 녹색
                borderRadius="50%"
                mr={1}
                sx={{
                  boxShadow: "0 0 8px #4CAF50", // 반짝이는 효과
                  animation: "pulse 2s infinite", // 움직이는 효과 (아래 스타일 참고)
                }}
              />
              <MDTypography variant="caption" fontWeight="bold" sx={{ color: "#344767" }}>
                AI 게시물 자동 생성 엔진 가동중
              </MDTypography>
            </MDBox>

            <MDBox width="1px" height="15px" bgcolor="#e0e0e0" />

            {/* 2. 인스타 연결 배지 (API 상태) */}
            <MDBox display="flex" alignItems="center" px={1.5}>
              <Icon sx={{ color: "#E1306C", mr: 0.5, fontSize: "16px !important" }}>link</Icon>
              <MDTypography variant="caption" fontWeight="bold" sx={{ color: "#344767" }}>
                인스타 연결됨
              </MDTypography>
            </MDBox>

            <MDBox width="1px" height="15px" bgcolor="#e0e0e0" />

            {/* 3. 트렌드 반영 상태 배지 옆에 키워드 추가 */}
            <MDBox display="flex" alignItems="center" px={1.5}>
              <Icon sx={{ color: "#1A73E8", mr: 0.8, fontSize: "16px !important" }}>
                auto_awesome
              </Icon>
              <MDBox>
                <MDTypography
                  variant="caption"
                  fontWeight="bold"
                  sx={{ color: "#344767", display: "block", lineHeight: 1 }}
                >
                  최신 트렌드 반영 완료
                </MDTypography>
                {/* 요게 핵심! 파이썬에서 받아올 실시간 키워드들 */}
                <MDTypography
                  variant="button"
                  sx={{ fontSize: "10px", color: "#1A73E8", fontWeight: "medium" }}
                >
                  #연말결산 #크리스마스마켓 #겨울코디
                </MDTypography>
              </MDBox>
            </MDBox>
          </MDBox>
        </MDBox>
        {/* 애니메이션 정의 (파일 하단에 이미 있다면 생략 가능) */}
        <style>
          {`
          @keyframes pulse {
            0% { opacity: 1; }
            50% { opacity: 0.4; }
            100% { opacity: 1; }
          }
        `}
        </style>
      </Toolbar>
    </AppBar>
  );
}

// Setting default values for the props of DashboardNavbar
DashboardNavbar.defaultProps = {
  absolute: false,
  light: false,
  isMini: false,
};

// Typechecking props for the DashboardNavbar
DashboardNavbar.propTypes = {
  absolute: PropTypes.bool,
  light: PropTypes.bool,
  isMini: PropTypes.bool,
};

export default DashboardNavbar;
