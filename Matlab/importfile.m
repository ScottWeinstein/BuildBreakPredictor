function [data,colheaders] = importfile(fileName)
% Import the file
nd = importdata(fileName);
data = nd.data;
colheaders = nd.colheaders;
% Create new variables in the base workspace from those fields.

