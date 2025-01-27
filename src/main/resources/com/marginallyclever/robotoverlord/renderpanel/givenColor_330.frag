// fragment shader
// draw only the color of the vertex
#version 330 core

in vec4 fragmentColor;
in vec3 normalVector;
in vec3 fragmentPosition;

out vec4 finalColor;

void main() {
    finalColor = fragmentColor;
}
